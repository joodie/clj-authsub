(ns authsub.middleware
  "clj-http compatible middleware for AuthSub requests")

(defn- add-header
  [req header value]
  (assoc-in req [:headers header] value))

(defn wrap-developer-key
  "Add a Google developer key the request object."
  [f developer-key]
  (fn [request]
    (add-header request "X-GData-Key" (str "key=" developer-key))))

(defn wrap-token
  "Add an autentication token to the request object"
  [f token]
  (fn [request]
    (add-header request "Authorization" (str "AuthSub token=\"" token "\""))))

(defn wrap-gdata-version
  "Add a GData-Version header to the request. Default version is 2."
  ([f version]
     (fn [request]
       (add-header request "GData-Version" version)))
  ([f]
     (wrap-gdata-version f "2")))

(defn wrap-host-fix
  "Fix up the host header to make the Google APIs happy.
Request must contain either a :url or a :server-name"
  [f]
  (fn [request]
    {:pre [(or (:url request)
               (:server-name request))]}
    (let [host (or (if (:url request)
                     (second (re-matches #"^.*?://([^/:]+).*" (:url request))))
                   (if (:server-name request)
                     (second (re-matches #"^([^:]+).*" (:server-name request))))
                   (throw (Exception. (str "Cannot get host name from request"))))
          headers (:headers request)]
      (assoc-in request [:headers "Host"] host))))
