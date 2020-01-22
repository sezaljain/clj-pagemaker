(ns converter.records
  (:require [converter.constants :as con]
            [converter.pagemaker-buffer :as pm6]))


                                        ;(def offset (atom 0))
                                        ;(def seq-no (atom 0))


(defn parse-header
  ;;get offset and length for the table of contents
  [file]
  (let [endian (if (= (seq (con/constants :endian-marker))
                      (seq (pm6/get-x-bytes-at-pos (:buffer file) 0x6 2)))
                 :big
                 :little)]
    (reset! (:endian file) endian)
    (reset!
     (:header file)
     {:toc-length (pm6/unpack file (con/constants :toc-length-offset) :short)
      :toc-offset (pm6/unpack file (con/constants :toc-offset-offset) :int)})))

(defn skip [number increment]
  (+ increment number))




;;if we do recur here??

;; (defn read-next-record-toc
;;                                         ;;this is working but is not independent of read-table-of-contents
;;   "returns updated records and offset"
;;   [input rec-location is-sub-rec sub-rec-type]
;;   (let [rec-type         (pm6/unpack input (+ 1 rec-location) :unsigned-char)
;;         num-recs         (pm6/unpack input (+ 2 rec-location) :short)
;;         offset           (pm6/unpack input (+ 4 rec-location) :int)
;;         get-sub-type     (and (not is-sub-rec)
;;                               (or (not= 0 rec-type) (= 0 num-recs)))
;;         sub-type         (if get-sub-type
;;                            (pm6/unpack input (+ 11 rec-location) :unsigned-char)
;;                            0)
;;         rec-size         (if get-sub-type 16 10)
;;         new-rec-location (if get-sub-type
;;                            (+ 16 rec-location)
;;                            (+ 10 rec-location))]
;;     (cond
;;       (and (= 0 rec-type) (= 0 offset))     (do (swap! seq-no inc) nil)
;;       (and (not is-sub-rec) (= 1 rec-type)) (let [rec (read-table-of-contents input offset num-recs true sub-type)]
;;                                               (swap! seq-no inc)
;;                                               rec)
;;       (and (not is-sub-rec) (= 0 rec-type)) (read-table-of-contents input offset num-recs false)
;;       (and (not= 0 num-recs) (not= 0 offset))
;;       (let [record-type (if (and is-sub-rec (not= rec-type sub-rec-type) (not= 0 sub-rec-type))
;;                           sub-rec-type
;;                           rec-type)
;;             rec         {:rec-type     record-type
;;                          :offset       offset
;;                          :get-sub-type get-sub-type
;;                          :num-recs     num-recs
;;                          :seq          @seq-no}]
;;         (if (not is-sub-rec) (swap! seq-no inc))
;;         rec))))


#_(defn read-next-record-toc
    ;;this is working with offset but not with seqnum as atom
    "returns updated records and offset"
    [input rec-loc no-of-recs is-sub-rec sub-rec-type]
    (loop [remaining-recs no-of-recs
           rec-location   rec-loc
           records        []]
      (if (= 0 remaining-recs)
        records
        (let [rec-type         (pm6/unpack input (+ 1 rec-location) :unsigned-char)
              num-recs         (pm6/unpack input (+ 2 rec-location) :short)
              offset           (pm6/unpack input (+ 4 rec-location) :int)
              get-sub-type     (and (not is-sub-rec)
                                    (or (not= 0 rec-type) (= 0 num-recs)))
              sub-type         (if get-sub-type
                                 (pm6/unpack input (+ 11 rec-location) :unsigned-char)
                                 0)
              rec-size         (if get-sub-type 16 10)
              new-rec-location (if get-sub-type
                                 (+ 16 rec-location)
                                 (+ 10 rec-location))]
          (cond
            (and (= 0 rec-type) (= 0 offset))     (do (swap! seq-no inc)
                                                      (recur
                                                       (dec remaining-recs)
                                                       new-rec-location
                                                       records))
            (and (not is-sub-rec) (= 1 rec-type)) (let
                                                      [rec (read-next-record-toc input offset num-recs true sub-type)]
                                                    (swap! seq-no inc)
                                                    (recur
                                                     (dec remaining-recs)
                                                     new-rec-location
                                                     (conj records rec)))
            (and (not is-sub-rec) (= 0 rec-type)) (recur
                                                   (dec remaining-recs)
                                                   new-rec-location
                                                   (conj records (read-next-record-toc input offset num-recs false 0)))
            (and (not= 0 num-recs) (not= 0 offset))
            (let [record-type (if (and is-sub-rec (not= rec-type sub-rec-type) (not= 0 sub-rec-type))
                                sub-rec-type
                                rec-type)
                  rec         {:rec-type     record-type
                               :offset       offset
                               :get-sub-type get-sub-type
                               :num-recs     num-recs
                               :seq          @seq-no}]
              (if (not is-sub-rec) (swap! seq-no inc))
              (recur
               (dec remaining-recs)
               new-rec-location
               (conj records rec)))
            :else                                 (recur (dec remaining-recs) new-rec-location records))))))
;;if subtype is there then size of record is 16 otherwise it is 10

#_(defn read-table-of-contents
    ([input offset- no-records is-sub-rec]
     (read-table-of-contents input offset- no-records is-sub-rec 0))
    ([input offset- no-records is-sub-rec sub-rec-type]
     (let [orig-offset     @offset
           min-record-size (if is-sub-rec 10 16)
           max-records     no-records]
       (when (and (not= no-records 0) (not= offset- 0))
         (reset! offset offset-)
         (let [recs
               (remove nil?
                       (vec (repeatedly
                             max-records
                             #(read-next-record-toc input offset- is-sub-rec sub-rec-type))))]
           (reset! offset orig-offset)
           recs)))))


;;turn this repeatedly into some reduce type fun

(defn read-next-record-toc
  "returns updated records and offset"
  [input rec-loc no-of-recs seq-num is-sub-rec sub-rec-type]
  (loop [remaining-recs no-of-recs
         rec-location   rec-loc
         seq-number     seq-num
         records        []]
    (prn remaining-recs,rec-location, seq-number (count records))
    (if (= 0 remaining-recs)
      (do (prn "sdf" remaining-recs) [seq-number records])
      (let [rec-type         (pm6/unpack input (+ 1 rec-location) :unsigned-char)
            num-recs         (pm6/unpack input (+ 2 rec-location) :short)
            offset           (pm6/unpack input (+ 4 rec-location) :int)
            get-sub-type     (and (not is-sub-rec)
                                  (or (not= 0 rec-type) (= 0 num-recs)))
            sub-type         (if get-sub-type
                               (pm6/unpack input (+ 11 rec-location) :unsigned-char)
                               0)
            rec-size         (if get-sub-type 16 10)
            new-rec-location (if get-sub-type
                               (+ 16 rec-location)
                               (+ 10 rec-location))]
        (cond
          (and (= 0 rec-type) (= 0 offset))     (recur
                                                 (dec remaining-recs)
                                                 new-rec-location
                                                 (inc seq-number)
                                                 records)
          (and (not is-sub-rec) (= 1 rec-type)) (let
                                                    [[sq-num rec] (read-next-record-toc
                                                                   input offset num-recs seq-number true sub-type)]
                                                  #_(swap! seq-no inc)
                                                  (recur
                                                   (dec remaining-recs)
                                                   new-rec-location
                                                   (inc sq-num)
                                                   (conj records rec)))
          (and (not is-sub-rec) (= 0 rec-type)) (let [[sq-num rec] (read-next-record-toc
                                                                    input offset num-recs false 0)]
                                                  (recur
                                                   (dec remaining-recs)
                                                   new-rec-location
                                                   sq-num
                                                   (conj records rec)))
          (and (not= 0 num-recs) (not= 0 offset))
          (let [record-type (if (and is-sub-rec (not= rec-type sub-rec-type) (not= 0 sub-rec-type))
                              sub-rec-type
                              rec-type)
                rec         {:rec-type     record-type
                             :offset       offset
                             :get-sub-type get-sub-type
                             :num-recs     num-recs
                             :seq          seq-number}]
            (recur
             (dec remaining-recs)
             new-rec-location
             (if (not is-sub-rec) (inc seq-number) seq-number)
             (conj records rec)))
          :else                                 (recur
                                                 (dec remaining-recs)
                                                 new-rec-location
                                                 seq-number
                                                 records))))))
