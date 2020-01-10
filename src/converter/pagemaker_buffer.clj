(ns converter.pagemaker-buffer
  ( :require [clojure.java.shell :as shell]
   [converter.buffer :as b]
   [converter.chanakya :as c]
   [clojure.java.io :as io])
  (:import [java.nio ByteBuffer ByteOrder])
  (:require [converter.constants :as con]))

(defn open-gsf
  "first checks if pagemaker format, then gives byte output based on microsoft ole format"
  [fname]
  (let [in         (b/init-stream fname)
        identifier (b/read-x-bytes-at-position in 0 8)
        is-ole     (= identifier [0xd0 0xcf 0x11 0xe0 0xa1 0xb1 0x1a 0xe1])]
    (if is-ole
      (-> (shell/sh "gsf" "cat" fname "PageMaker" :out-enc :bytes)
          :out)
      (prn "invalid file format for pagemaker"))))

(defn get-x-bytes-at-pos [input pos x-bytes]
  (java.util.Arrays/copyOfRange input pos (+ pos x-bytes)))

#_(defn unpack [input pos x-bytes endian]
    (let [;fmt-length
          byte-arr    (get-x-bytes-at-pos input pos x-bytes)
          endian-val  (if (= :little endian)
                        java.nio.ByteOrder/LITTLE_ENDIAN
                        java.nio.ByteOrder/BIG_ENDIAN)
          byte-buffer (-> (. ByteBuffer wrap byte-arr)
                          (.order endian-val))]
                                        ;   (prn "byte-array" (.toString byte-arr))
      (cond
        (= 1 x-bytes) (.get byte-buffer)
        (= 2 x-bytes) (.getShort byte-buffer)
        (= 4 x-bytes) (.getInt byte-buffer))))
(defn normalize-byte [b]
  (if (>= b 0)
    b
    (+ 255 b)))

(defn unpack
  ([input pos fmt endian number]
   (for [i (range number)]
     (unpack input (+ (* i (con/fmts fmt))  pos) fmt endian)))
  ([input pos fmt endian]
   (let [x-bytes     (con/fmts fmt)
         byte-arr    (get-x-bytes-at-pos input pos x-bytes)
         endian-val  (if (= :little endian)
                       java.nio.ByteOrder/LITTLE_ENDIAN
                       java.nio.ByteOrder/BIG_ENDIAN)
         byte-buffer (-> (. ByteBuffer wrap byte-arr)
                         (.order endian-val))]
     (cond
       (= 1 x-bytes) (normalize-byte (.get byte-buffer))
       (= 2 x-bytes) (.getShort byte-buffer)
       (= 4 x-bytes) (.getInt byte-buffer)))))


(defn hex-to-ascii [hex-pair]
  (let [h (if (= java.lang.String (type hex-pair))
            (Integer/parseInt hex-pair 16)
            hex-pair)]
    (char (if (>= h 0)
            h
            (+ 255 h)))))

(defn string-to-ascii [s]
  (let [s-pairs (re-seq #".{1,2}" s)]
    (clojure.string/join (hex-to-ascii s-pairs))))

(defn transform-text [text]
  (-> text
      string-to-ascii
      c/chanakya-to-unicode))
