(ns converter.core
  (:require [clojure.java.io :as io]
            [clojure.data :as data]
            [converter.offsets :as offsets]
            [clojure.java.shell :only [sh]])
  (:gen-class))

#_(defn -main
    "I don't do a whole lot ... yet."
    [& args]
    (slurp "../eng.p65")
    (println "Hello, World!"))



(def file "../2arial.p65")
(def f (io/input-stream file))
(def offset (atom 0))

(defn debug [x-]
  (let [off (dec @offset)]
    (when (= 0 (mod off 16))
      (do (prn) (prn (format "%x" off))))
    (print (format " %x " x-))
    x-))

(defn read-stream [stream] ;; 1 byte at a time uint 8
  (swap! offset inc)
  (debug (.read stream)))

(defn read-stream-section [stream section-length]
  (for [p (range section-length)]
    (read-stream stream)))

(defn read-stream-u16 [stream]
  (read-stream-section stream 2))

(defn read-stream-u32 [stream]
  (read-stream-section stream 4))

(defn read-x-bytes-at-offset [stream read-offset x]
                                        ;  (prn "requested read offset" read-offset)
  (if (> @offset read-offset)
    (prn "cant read as we are past offset, returning")
    (do (doall (read-stream-section stream (- read-offset @offset)))
        (cond
          (= x 4) (read-stream-u32 stream)
          (= x 2) (read-stream-u16 stream)
          (= x 1) (read-stream stream)
          :else   (read-stream-section stream x)))))



(defn hexlify-string [s]
  (map-indexed
   (fn [idx itm]
     [(format "%x" idx) (str itm)]) s))


(defn hex [c] (Integer/toHexString c))

(defn difference [s1 s2]
  (reduce
   (fn [acc m]
     (conj acc [(first (:a m)) {:a (second (:a m)) :b (second (:b m))}]))
   []
   (remove
    nil?
    (map
     #(when (not= %2 %3)
        {:a [ (hex %1) %2]
         :b [ (hex %1) %3]})
     (range)
     s1 s2))))

(defn read-x-bytes-at-hex-loc [s loc no-bytes]
  (subs s loc (+ no-bytes loc)))


(def a (read-x-bytes-at-offset f 0x30 4)) ;; 00
#_(def b (read-x-bytes-at-offset f 0x30 4))      ;;0x1000
;; input->tell is 0x34
;;subrecords is false -> minrecordsize 16
;; currently at offset
#_(def c (read-x-bytes-at-offset f 0x1000 10))
