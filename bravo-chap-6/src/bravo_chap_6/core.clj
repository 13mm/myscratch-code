(ns bravo-chap-6.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (println "Hello, World!")
  (println "Hello, World!")
  )

(-main)

(defn fact
  ([n] (fact n 1))
  ([n accu]
    (if (zero? n)
      accu
      (recur (dec n) (* accu n) ))
    )
)

(def x)

(defn test1 [x] x)


