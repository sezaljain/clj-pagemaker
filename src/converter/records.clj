(ns converter.records
  (:require [converter.constants :as con]
            [converter.pagemaker-buffer :as pm6]))

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

(defn read-next-record-toc
  "returns updated records and offset"
  [input rec-loc no-of-recs seq-num is-sub-rec sub-rec-type]
  (prn :offset rec-loc seq-num)
  (loop [remaining-recs no-of-recs
         rec-location   rec-loc
         seq-number     seq-num
         records        []]
    ;;    (prn remaining-recs,rec-location, seq-number (count records))
    (if (= 0 remaining-recs)
      [seq-number records]
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
                                                  (recur
                                                   (dec remaining-recs)
                                                   new-rec-location
                                                   (inc sq-num)
                                                   (conj records rec)))
          (and (not is-sub-rec) (= 0 rec-type)) (let [[sq-num rec] (read-next-record-toc
                                                                    input offset num-recs seq-number false 0)]
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
                                                 (inc seq-number)
                                                 records))))))
;;;parsing table of contents -- issue some are missing
;;we have offset and length to begin with (lenght 80, offset 125120)
;;length is supposed to be no of records, offset is supposed to be record location
;;if () no of records = 0 and offset = 0 --> return. no records to read
;;you calculate max no of records => buffer-length - offset(rec location)
;; looop through this number (but no new number is passed to func in loop
