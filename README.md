# authsub

Client library for using Google AuthSub authentication as used by youtube, gmail etc.

## Usage

  (use 'authsub.core)
  
  ;; first...
  (redirect-user-to (auth-sub-uri :next-uri "http://my-site/get-token"
                                  :session true
                                  :scope "http://gdata.youtube.com"))

  ;; assuming token now contains the returned (single use) token
  (def session-token (get-session-token developer-key token))

  ;; use session-token to fetch feed including private videos
  (def user-feed (get-with-token 
                   "http://gdata.youtube.com/feeds/api/users/default/uploads"))
          
## License

Copyright (C) 2011 Joost Diepenmaat, Zeekat Softwareontwikkeling

Distributed under the Eclipse Public License, the same as Clojure.
