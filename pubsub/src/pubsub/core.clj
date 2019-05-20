(ns pubsub.core
  (:require [clojure.core.async :as async :refer [chan <! >! timeout
                                                  pub sub unsub unsub-all go-loop]])
  (:gen-class))

; publisher is just a normal channel
(def publisher (chan))

; publication is a thing we subscribe to
(def publication
  (pub publisher #(:topic %)))

; define a bunch of subscribers
(def subscriber-one (chan))
(def subscriber-two (chan))
(def subscriber-three (chan))

; subscribe
(sub publication :account-created subscriber-one)
(sub publication :account-created subscriber-two)
(sub publication :user-logged-in  subscriber-two)
(sub publication :change-page     subscriber-three)

(defn take-and-print [channel prefix]
  (go-loop []
           (println prefix ": " (<! channel))
           (recur)))

(take-and-print subscriber-one "subscriber-one")
(take-and-print subscriber-two "subscriber-two")
(take-and-print subscriber-three "subscriber-three")

(def a "mohan")

(defn producer [p-channel]
  (go-loop [n 1]
    (>! p-channel n)
    (<! (timeout 500))
    (recur (+ n 2))
  )
)

(defn producer-even [p-channel]
  (go-loop [n 0]
    (>! p-channel n)
    (<! (timeout 1000))
    (recur (+ n 2))
    )
  )


(defn consumer [c]
  (go-loop []
    (let [v (<! c)]
      (println "out : " v)
    )
    (recur)
  )
)

(defn consume-on-multi-channels [x & c]
  (go-loop []
    (let [[v port] (async/alts! c)]
      (println "out : " v)
      )
    (recur)
    )
  )

(def producer-chan (chan))
(def producer-chan-2 (chan))
(producer producer-chan)
(producer-even producer-chan-2)
(consume-on-multi-channels nil producer-chan producer-chan-2)









