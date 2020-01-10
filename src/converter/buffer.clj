(ns converter.buffer)


(def offset (atom 0))

(defn init-stream [name]
  (reset! offset 0)
  (clojure.java.io/input-stream name))

(defn debug [x-]
  (let [off @offset]
    (when (= 0 (mod off 16))
      (do (prn) (prn (format "%x" off))))
    (print (format " %x " x-))
    x-))

(defn read-stream [stream] ;; 1 byte at a time uint 8
  (let [b (.read stream)]
    (swap! offset inc)
    b))

#_(defn read-stream-section [stream section-length]
    (for [p (range section-length)]
      (read-stream stream)))

#_(defn read-stream-u16 [stream]
    (read-stream-section stream 2))

#_(defn read-stream-u32 [stream]
    (read-stream-section stream 4))

#_(defn read-x-bytes-at-offset [stream read-offset x]
                                        ;  (prn "requested read offset" read-offset)
    (if (> @offset read-offset)
      (prn "cant read as we are past offset, returning")
      (do (doall (read-stream-section stream (- read-offset @offset)))
          (cond
            (= x 4) (read-stream-u32 stream)
            (= x 2) (read-stream-u16 stream)
            (= x 1) (read-stream stream)
            :else   (read-stream-section stream x)))))

(defn skip-to-offset [stream pos]
  (reset! offset pos)
  (.skip stream pos))

(defn read-x-bytes [stream x]
  (for [p (range x)]
    (read-stream stream)))

(defn read-x-bytes-at-position [stream pos x]
  (if (> @offset pos)
    (prn "cant read")
    (do (skip-to-offset stream pos)
        (read-x-bytes stream x))))

(defn little-endian [arr]
  (reverse arr))
