(ns authsub.core
   (:require [clj-http.client :as http]
             [clj-http.core :as http-core])
   (:import java.net.URLEncoder))

(defn- enc
  [s]
  (URLEncoder/encode s))

(defn auth-sub-uri
  "Make a uri for requesting an AuthSub token. The user can follow this url
and if she authorizes, next-url is called with the parameter \"token\" appended."
  [& {:keys [next-url secure session scope]}]
  (when secure
    (throw (Exception. "Secure AuthSubRequests are currently not supported")))
  (str "https://www.google.com/accounts/AuthSubRequest?scope=" (enc scope)
       "&session=" (if session 1 0) "&secure=" (if secure 1 0) "&next=" (enc next-url)))


(def session-token-uri
  "https://www.google.com/accounts/AuthSubSessionToken")

(defn token-info-uri
  "https://www.google.com/accounts/AuthSubTokenInfo")

(def request
  (http/wrap-url http-core/request))

(defn get-with-token
  "Get a GData url with a given token and developer-key. returns the full response object"
  [developer-key token url]
  ;;
  ;; we set our own Host: header without a port number, since the
  ;; default client includes the port. That is, it sets "Host:
  ;; hostname:80" or "hostname:443", and the google data API doesn't
  ;; like that
  ;;
  (let [host (or (second (re-matches #".*?://([^/:]+).*" url))
                 (throw (Exception. (str "Cannot get host name from url '" url "'"))))
        resp (request {:request-method :get
                       :url url
                       :headers {"Host" host
                                 "Authorization" (str "AuthSub token=\"" token "\"")
                                 "X-GData-Key" (str "key=" developer-key)
                                 "GData-Version" "2.1"}})]
    resp))

(defn get-session-token
  "get a long-lived session token from a single-use token. the single-use
token must have been requested with session true"
  [developer-key token]
  (let [r (get-with-token developer-key token session-token-uri)]
    (if (= 200 (:status r))
      (if-let [[_ token] (re-matches #"Token=(\S+)\n" (String. (:body r)))]
        token
        (throw (Exception. (print-str r))))
      (throw (Exception. (print-str r))))))

(defn get-token-info
  "Fetch information for the given token"
  [developer-key token]
  (:body (get-with-token developer-key token token-info-uri)))

