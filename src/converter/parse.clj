(ns converter.parse
  (:require [converter.pagemaker-buffer :as pm6]
            [converter.records :as rec]
            [converter.chanakya :as c]
            [converter.constants :as con]))


(reset! rec/offset 0)
(reset! rec/seq-no 0)

(def buf (pm6/open-gsf "../test.p65"))

(def buf-values (rec/parse-header buf))
(def endian (:endian buf-values))

(defn add-type-detail [x]
  (if (seq? x)
    (map add-type-detail x)
    (assoc
     x
     :rec-type-name
     (-> (:rec-type x)
         con/pmd-rec-types
         :name))))

(def rr (map add-type-detail (rec/read-table-of-contents buf  (:endian buf-values) (:toc-offset buf-values) (:toc-length buf-values) false)))

(defn cmyk-to-rgb [c m y k]
  ;;; TODO to be tested, definitely doesnt seem correct
  (let [max-u16 100]
    (map #(int (* 255 (min 1.0 (/ (+ % k) max-u16)))) [c m y]))
  )

(defn parse-dims [offset]
  (let [dims [(pm6/unpack buf offset :short endian) (pm6/unpack buf (+ 2 offset) :short endian)]]
    (if (= endian :little)
      dims
      (reverse dims))))

(defn parse-rule [offset]
  (let [flags (pm6/unpack buf offset :short endian)]
    (if (not= 0 (bit-and flags 0x1))
      {:stroke-flags          flags
       :stroke-type           (pm6/unpack buf (+ 2 offset) :char endian)
       :stroke-transparent-bg (pm6/unpack buf (+ 3 offset) :char endian)
       :stroke-width          (pm6/unpack buf (+ 4 offset) :int endian)
       :stroke-color          (pm6/unpack buf (+ 6 offset) :short endian)
       :stroke-tint           (pm6/unpack buf (+ 8 offset) :short endian)}
      {})))

(defn interpret-char-flags [flags]
  {:bold       (bit-and flags 0x1)
   :italic     (bit-and flags 0x2)
   :underline  (bit-and flags 0x4)
   :outline    (bit-and flags 0x8)
   :shadow     (bit-and flags 0x10)
   :strike     (bit-and flags 0x100)
   :super      (bit-and flags 0x200)
   :sub        (bit-and flags 0x400)
   :all-caps   (bit-and flags 0x800)
   :small-caps (bit-and flags 0x1000)})

(defmulti parse-record
  (fn [record]
    (:rec-type record)))

(defmethod parse-record :default [record]
  record
  #_(prn "do nothing" (:rec-type-name record)))

(defmethod parse-record
  ;; parsing global info
  0x18
  [record]
  (let [offset         (:offset record)
        [left top]     (parse-dims (+ 54 offset))
        [right bottom] (parse-dims (+ 54 4 offset))
        some-index     (map pm6/hex-to-ascii (pm6/unpack buf (+ 1232 offset) :char endian 16))]
    {(:rec-type-name record)
     {:double-sided   (bit-and
                       (if (= :little endian) 0x2 0x40)
                       (pm6/unpack buf offset :unsigned-char endian))
      :no-of-pages    (pm6/unpack buf (+ 48 offset) :short endian)
      :cur-page       (pm6/unpack buf (+ 46 offset) :short endian)
      :start-page     (pm6/unpack buf (+ 50 offset) :short endian)
      :page-width     (/ (- right left) 1440)
      :page-height    (/ (- bottom top) 1440)
      :page-numbering (con/numbering_map (pm6/unpack buf (+ 1231 offset) :char endian))
      :margins        (pm6/unpack buf (+ offset 2222) :short :little 4)}}))

(defmethod parse-record 0x13 [record]
  ;; parsing fonts
  (loop [offset   (:offset record)
         fontname ""]
    (let [c (pm6/unpack buf offset :char endian)]
      (if (not= 0 c)
        (recur (inc offset) (str fontname (char c)))
        fontname))))


(defmethod parse-record 0x15 [record]
  ;; parsing colors
  (prn "parsing colros")
  (let [offset      (+ 0x22 (:offset record))
        color-model (pm6/unpack buf offset :unsigned-char endian)
        rgb         (if (= :rgb (con/color-rec-types color-model))
                      (pm6/unpack buf (+ 3 offset) :unsigned-char endian 3)
                      (apply cmyk-to-rgb (pm6/unpack buf (+ 3 offset) :short endian 4)))]

    (prn (con/color-rec-types color-model))
    {(:rec-type-name record)
     {:rgb         rgb
      :color-model (con/color-rec-types color-model)}}))


(defmethod parse-record 0x05 [record]
  ;; parsing pages
  (let [offset  (:offset record)
        seq-num (pm6/unpack buf (+ 2 offset) :short endian)]
    (prn "to parse shape " seq-num))
  )


(defmethod parse-record 0x0d [record]
  ;;parse text
  (clojure.string/join
   (map char (pm6/unpack buf (:offset record) :char endian (:num-recs record)))))


(defmethod parse-record 0x1c [record]
  ;;parse char properties
  (let [offset                       (:offset record)
        [length font-face font-size] (pm6/unpack buf offset :short endian 3)
        flags                        (pm6/unpack buf (+ 10 offset) :short endian)]
    (conj {:length         length
           :font-face      font-face
           :font-size      font-size
           :font-color     (pm6/unpack buf (+ 8 offset) :short endian)
           :kerning        (pm6/unpack buf (+ 16 offset) :short endian )
           :super-sub-size (pm6/unpack buf (+ 20 offset) :short endian )
           :sub-pos        (pm6/unpack buf (+ 22 offset) :short endian )
           :super-pos      (pm6/unpack buf (+ 24 offset) :short endian )
           :tint           (pm6/unpack buf (+ 28 offset) :short endian )}
          (interpret-char-flags flags))))


(defmethod parse-record 0x0b [record]
  ;;parse paragraph properties
  (let [offset    (:offset record)
        flags     (pm6/unpack buf (+ 2 offset) :char endian)
        keep-opts (pm6/unpack buf (+ 40 offset) :short endian)]
    {:length         (pm6/unpack buf offset :short endian)
     :hyphenate      (bit-and flags 0x8)
     :align          (pm6/unpack buf (+ 3 offset) :char endian)
     :left-indent    (pm6/unpack buf (+ 10 offset) :short endian)
     :first-indent   (pm6/unpack buf (+ 12 offset) :short endian)
     :right-indent   (pm6/unpack buf (+ 14 offset) :short endian)
     :before-indent  (pm6/unpack buf (+ 16 offset) :short endian)
     :after-indent   (pm6/unpack buf (+ 18 offset) :short endian)
     :hyphen-count   (pm6/unpack buf (+ 38 offset) :char endian)
     :keep-together  (bit-and keep-opts 0x1)
     :keep-with-next (bit-and (bit-shift-right keep-opts 1) 0x3)
     :widows         (bit-and (bit-shift-right keep-opts 4) 0x3)
     :orphans        (bit-and (bit-shift-right keep-opts 7) 0x3)
     :rule-above     (parse-rule (+ 44 offset))
     :rule-below     (parse-rule (+ 62 offset))})
  )

(defn get-records-of-seq-nos [seq-nos]
  (filter #(not= -1 (.indexOf seq-nos (:seq %))) rr))

(defmethod parse-record 0x1a [record]
  ;; parsing a text block
  (let [offset                   (:offset record)
        text-block-id            (pm6/unpack buf (+ 0x20 offset) :int endian)
        text-box-related-records (pm6/unpack buf (+ 0x24 offset) :short endian 6)]
    (reduce
     (fn [acc rec]
       (assoc
        acc
        (:rec-type-name rec)
        (parse-records rec)))
     {}
     (get-records-of-seq-nos text-box-related-records))))

(defmethod parse-record 0x28 [record]
  ;;parsing an xform
  (let [offset (:offset record)]
    {:rotation-degree (pm6/unpack buf offset :int endian)
     :skew-degree     (pm6/unpack buf offset :int endian)
     :top-left        (parse-dims (+ 10 offset))
     :bot-right       (parse-dims (+ 14 offset))
     :rotating-point  (parse-dims (+ 18 offset))
     :id              (pm6/unpack buf (+ 22 offset) :int endian)}))

(defmulti parse-shape
  (fn [record]
    (pm6/unpack buf (:offset record) :char endian)))

(defmethod parse-shape 0x01 [record]
  ;;parsing a text box shape
  (let [offset             (:offset record)
        text-box-block-id  (pm6/unpack buf (+ 32 offset) :char endian)
        text-block-records (flatten (get-expanded-records-of-type 0x1a))]
    (parse-record
     (first
      (filter
       #(= text-box-block-id
           (pm6/unpack buf (+ 32 (:offset %)) :int endian))
       text-block-records)))))

(defmethod parse-shape 0x03 [record]
  ;;parse line shape
  (let [offset     (:offset record)
        mirror-var (pm6/unpack buf (+ 38 offset) :short endian)]
    {:bbox
     {:top-left     (parse-dims (+ 6 offset))
      :bottom-right (parse-dims (+ 10 offset))}
     :mirrored (if (or (= mirror-var 257) (= mirror-var 0)) false true)
     :stroke-props
     {:type       (pm6/unpack buf (+ 46 offset) :char endian)
      :width      (pm6/unpack buf (+ 48 offset) :short endian)
      :color      (pm6/unpack buf (+ 4 offset) :char endian)
      :over-print (pm6/unpack buf (+ 58 offset) :char endian)
      :tint       (pm6/unpack buf (+ 51 offset) :char endian)}}))

(defmethod parse-shape 0x04 [record]
  ;;parse rectangle shape
  (let [offset (:offset record)]
    {:fill-props
     {:type       (pm6/unpack buf (+ 38 offset) :char endian)
      :over-print (pm6/unpack buf (+ 2 offset) :char endian)
      :color      (pm6/unpack buf (+ 4 offset) :char endian)
      :tint       (pm6/unpack buf (+ 224 offset) :char endian)}
     :bbox
     {:top-left     (parse-dims (+ 6 offset))
      :bottom-right (parse-dims (+ 10 offset))}
     :stroke-props
     {:type       (pm6/unpack buf (+ 32 offset) :char endian)
      :width      (pm6/unpack buf (+ 35 offset) :short endian)
      :color      (pm6/unpack buf (+ 40 offset) :char endian)
      :over-print (pm6/unpack buf (+ 42 offset) :char endian)
      :tint       (pm6/unpack buf (+ 44 offset) :char endian)}}))

(defmethod parse-shape 0x05 [record]
  ;;parse ellipse shape
  (let [offset (:offset record)]
    {:fill-props
     {:type       (pm6/unpack buf (+ 38 offset) :char endian)
      :over-print (pm6/unpack buf (+ 2 offset) :char endian)
      :color      (pm6/unpack buf (+ 4 offset) :char endian)
      :tint       (pm6/unpack buf (+ 224 offset) :char endian)}
     :bbox
     {:top-left     (parse-dims (+ 6 offset))
      :bottom-right (parse-dims (+ 10 offset))}
     :stroke-props
     {:type       (pm6/unpack buf (+ 32 offset) :char endian)
      :width      (pm6/unpack buf (+ 35 offset) :short endian)
      :color      (pm6/unpack buf (+ 40 offset) :char endian)
      :over-print (pm6/unpack buf (+ 42 offset) :char endian)
      :tint       (pm6/unpack buf (+ 44 offset) :char endian)}}))


(defmethod parse-shape 0x06 [record] (prn "this is a bitmap"))
(defmethod parse-shape 0x0a [record] (prn "this is a metafile"))

(defmethod parse-shape 0x0c [record]
  ;;parse polygon shape
  (let [offset            (:offset record)
        line-set-seq-nums (pm6/unpack buf (+ 46 offset) :short endian)]
    {:fill-props
     {:type       (pm6/unpack buf (+ 38 offset) :char endian)
      :over-print (pm6/unpack buf (+ 2 offset) :char endian)
      :color      (pm6/unpack buf (+ 4 offset) :char endian)
      :tint       (pm6/unpack buf (+ 224 offset) :char endian)}
     :bbox
     {:top-left     (parse-dims (+ 6 offset))
      :bottom-right (parse-dims (+ 10 offset))}
     :points (map #(parse-dims (:offset %)) (get-records-of-seq-no line-set-seq-nums))
     :closed (not= 1 (pm6/unpack buf (+ 56 offset) :char endian))
     :stroke-props
     {:type       (pm6/unpack buf (+ 32 offset) :char endian)
      :width      (pm6/unpack buf (+ 35 offset) :short endian)
      :color      (pm6/unpack buf (+ 40 offset) :char endian)
      :over-print (pm6/unpack buf (+ 42 offset) :char endian)
      :tint       (pm6/unpack buf (+ 44 offset) :char endian)}}))


(defmethod parse-record 0x19 [record]
  ;;parse shapes
  (assoc
   {:xform-id   (pm6/unpack buf (+ 28 (:offset record)) :int endian)
    :shape-name (con/shape-record-types (pm6/unpack buf (:offset record) :char endian))}
   :shape-data
   (parse-shape record)))


(defn get-records-of-type [type]
  (filter #(= (:rec-type %) type) rr))

(defn get-expandend-records [record]
  (let [rec-to-copy record]
    (for [i (range (:num-recs record))]
      (-> rec-to-copy
          (assoc :num-recs 1)
          (assoc :offset  (+ (:offset record) (* i (-> (:rec-type record) con/pmd-rec-types :size))))))))

(defn get-expanded-records-of-type [type]
  (map get-expandend-records (get-records-of-type type)))

(defn parse-records [record]
  (if (= 0x0d (:rec-type record))
    (parse-record record)
    (for [i (range (:num-recs record))]
      (parse-record (update record :offset + (* i (-> (:rec-type record) con/pmd-rec-types :size)))))))









;; 2 figure out the final output tree this is going into
;; time to fix the sequence no of things
