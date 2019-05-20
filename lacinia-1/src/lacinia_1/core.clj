(ns lacinia-1.core
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.pedestal :refer [service-map]]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.util :as util]
    [clojure.core.async :as async :refer [chan <! >! timeout
                                                    pub sub unsub unsub-all go-loop]]
    [lacinia-1.StockMarketAccess :as ma]
    [io.pedestal.http :as http]))

(defn ^:private resolve-hello
  [context args value]
  context)


(defn producer [p-channel]
  (go-loop [n 1]
    (>! p-channel n)
    (<! (timeout 5000))
    (recur (+ n 2))
    )
  )

(def producer-chan (chan))


(defn log-message-streamer
  [context args source-stream]
  ;; Create an object for the subscription.
    (go-loop []
      (let [v (<! producer-chan)]
        (source-stream v)
        )
      (recur)
      )
    ;; Return a function to cleanup the subscription
    #(println "closing connection"))

(defn watch-stock [context args source-stream]
   (let [listener-chan (chan)]
     (println args)
     (println (-> args :ric keyword))
     (println (-> args :ric keyword type))
     (ma/watch-stock (-> args :ric keyword) listener-chan)
     (go-loop []
       (let [v (<! listener-chan)]
         (source-stream v)
         )
       (recur)
       )
     )
  #(println "closing connection")
   )

(defn ^:private hello-schema
  []
  (-> "hw-schema.edn"
      io/resource
      slurp
      edn/read-string
      (util/attach-resolvers {:resolve-hello resolve-hello})
      (util/attach-streamers {:ping-response log-message-streamer
                              :stock-quote watch-stock})
      schema/compile))

(producer producer-chan)
(ma/stock-price-updater)


(def service (-> (hello-schema)
                 (service-map {:graphiql true :subscriptions true})
                 http/create-server
                 http/start))





