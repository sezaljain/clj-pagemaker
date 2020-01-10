(ns converter.records
  (:require [converter.constants :as con]
            [converter.pagemaker-buffer :as pm6]))


(def offset (atom 0))
(def seq-no (atom 0))

(def file {:filename    "../test.p65"
           :buffer      (pm6/open-gsf "../test.p65")
           :offset      (atom 0)
           :seq-no      (atom 0)
           :records     (atom [])
           :parsed-data (atom {})})

(defn parse-header
  ;;get offset and length for the table of contents
  [buf]
  (let [endian (if (= (seq (con/constants :endian-marker))
                      (seq (pm6/get-x-bytes-at-pos buf 0x6 2)))
                 :big
                 :little)
        length (pm6/unpack buf (con/constants :toc-length-offset) :short endian)
        offset (pm6/unpack buf (con/constants :toc-offset-offset) :int endian)]
    {:endian     endian
     :toc-length length
     :toc-offset offset}))

(defn skip [number increment]
  (+ increment number))

(defn read-next-record-toc
                                        ;what should this return
  "returns updated records and offset"
  [buf endian is-sub-rec sub-rec-type]
  (let [rec-type     (pm6/unpack buf (swap! offset skip 1) :unsigned-char endian)
        num-recs     (pm6/unpack buf (swap! offset skip 1) :short endian)
        offset-      (pm6/unpack buf (swap! offset skip 2) :int endian)
        get-sub-type (and (not is-sub-rec)
                          (or (not= 0 rec-type) (= 0 num-recs)))
        sub-type     (if get-sub-type
                       (pm6/unpack buf (swap! offset skip (+ 4 2 1)) :unsigned-char endian)
                       0)
        new-offset   (if get-sub-type
                       (swap! offset skip 5)
                       (swap! offset skip 6))]
    (cond
      (and (= 0 rec-type) (= 0 offset-))    (do (swap! seq-no inc) nil)
      (and (not is-sub-rec) (= 1 rec-type)) (let [rec (read-table-of-contents buf endian offset- num-recs true sub-type)]
                                              (swap! seq-no inc)
                                              rec)
      (and (not is-sub-rec) (= 0 rec-type)) (read-table-of-contents buf endian offset- num-recs false)
      (and (not= 0 num-recs) (not= 0 offset-))
      (let [record-type (if (and is-sub-rec (not= rec-type sub-rec-type) (not= 0 sub-rec-type))
                          sub-rec-type
                          rec-type)
            rec         {:rec-type     record-type
                         :offset       offset-
                         :get-sub-type get-sub-type
                         :num-recs     num-recs
                         :seq          @seq-no}]
        (if (not is-sub-rec) (swap! seq-no inc))
        rec))))


(defn read-table-of-contents
  ([buf endian offset- no-records is-sub-rec]
   (read-table-of-contents buf endian offset- no-records is-sub-rec 0))
  ([buf endian offset- no-records is-sub-rec sub-rec-type]
   (let [orig-offset     @offset
         min-record-size (if is-sub-rec 10 16)
         max-records     no-records]
     (when (and (not= no-records 0) (not= offset- 0))
       (reset! offset offset-)
       (let [recs
             (remove nil?
                     (vec (repeatedly
                           max-records
                           #(read-next-record-toc buf endian is-sub-rec sub-rec-type))))]
         (reset! offset orig-offset)
         recs)))))


(defn parse-table-of-contents
  [buf offset length]
  (read-table-of-contents buf :little offset length false))
