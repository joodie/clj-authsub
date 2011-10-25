(ns authsub.test.middleware
  (:use clojure.test
        authsub.middleware)
 (:require [clj-http.client :as http]))

(deftest test-pre
  (is (map? ((-> identity
                 http/wrap-url)
             {:url "http://example.com"}))))

(deftest test-middleware
  (let [mk-req (-> identity
                   http/wrap-url
                   (wrap-developer-key "devkey")
                   (wrap-token "token")
                   wrap-host-fix)
        req (mk-req {:request-method :post
                     :url "http://example.com:8023/"})]
    (is (get-in req [:headers "Host"]) "example.com")
    (is (get-in req [:headers "Authorization"] "AuthSub token=\"token\""))
    (is (get-in req [:headers "X-GData-Key"]  "key=devkey"))))
