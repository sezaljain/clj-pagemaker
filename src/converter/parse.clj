(ns converter.parse
  (:require [converter.pagemaker-buffer :as pm6]
            [converter.records :as rec]
            [converter.chanakya :as c]
            [converter.constants :as con]
            [converter.parse :as parse]
            [converter.matcher :as matcher]))


#_(reset! rec/offset 0)
#_(reset! rec/seq-no 0)

#_(def buf (pm6/open-gsf "../test.p65"))

#_(def buf-values (rec/parse-header buf))
#_(def endian (:endian buf-values))

(defn add-type-detail [x]
  (if (vector? x)
    (map add-type-detail x)
    (assoc
     x
     :rec-type-name
     (-> (:rec-type x)
         con/pmd-rec-types
         :name))))

#_(def rr (map add-type-detail (rec/read-table-of-contents buf  (:endian buf-values) (:toc-offset buf-values) (:toc-length buf-values) false)))

(defn cmyk-to-rgb [c m y k]
  ;;; TODO to be tested, definitely doesnt seem correct
  (let [max-u16 100]
    (map #(int (* 255 (min 1.0 (/ (+ % k) max-u16)))) [c m y]))
  )

(defn get-records-of-type [input type]
  (filter #(= (:rec-type %) type) @(:records input)))

(defn get-expandend-records [record]
  (let [rec-to-copy record]
    (for [i (range (:num-recs record))]
      (-> rec-to-copy
          (assoc :num-recs 1)
          (assoc :offset  (+ (:offset record) (* i (-> (:rec-type record) con/pmd-rec-types :size))))))))

(defn get-expanded-records-of-type [input type]
  (map get-expandend-records (get-records-of-type input type)))

(defn get-records-of-seq-no [input seq-no]
  (first (filter #(= seq-no (:seq %)) @(:records input))))

(defn get-records-of-seq-nos [input seq-nos]
  (filter #(not= -1 (.indexOf seq-nos (:seq %))) @(:records input)))

(defn parse-dims [input offset]
  (let [dims [(pm6/unpack input offset :short) (pm6/unpack input (+ 2 offset) :short)]]
    (if (= (:endian input) :little)
      dims
      (reverse dims))))

(defn parse-rule [input offset]
  (let [flags (pm6/unpack input offset :short)]
    (if (not= 0 (bit-and flags 0x1))
      {:stroke-flags          flags
       :stroke-type           (pm6/unpack input (+ 2 offset) :char)
       :stroke-transparent-bg (pm6/unpack input (+ 3 offset) :char)
       :stroke-width          (pm6/unpack input (+ 4 offset) :int)
       :stroke-color          (pm6/unpack input (+ 6 offset) :short)
       :stroke-tint           (pm6/unpack input (+ 8 offset) :short)}
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
  (fn [input record]
    (:rec-type record)))

(defmethod parse-record :default [input record]
  #_record
  (prn "do nothing" (:rec-type record) (:rec-type-name record) record))

(defn parse-records [input record]
  (if (seq? record)
    (map #(parse-records input %) record)
    (when (con/pmd-rec-types (:rec-type record))
      (if (= 0x0d (:rec-type record))
        (parse-record input record)
        (for [i (range (:num-recs record))]
          (parse-record input (update record :offset + (* i (-> (:rec-type record) con/pmd-rec-types :size)))))))))

(defmethod parse-record
  ;; parsing global info
  0x18
  [input record]
  (let [offset         (:offset record)
        [left top]     (parse-dims input (+ 54 offset))
        [right bottom] (parse-dims input (+ 54 4 offset))
        some-index     (map pm6/hex-to-ascii (pm6/unpack input (+ 1232 offset) :char 16))]
    {(:rec-type-name record)
     {:double-sided   (bit-and
                       (if (= :little (:endian input)) 0x2 0x40)
                       (pm6/unpack input offset :unsigned-char))
      :no-of-pages    (pm6/unpack input (+ 48 offset) :short)
      :cur-page       (pm6/unpack input (+ 46 offset) :short)
      :start-page     (pm6/unpack input (+ 50 offset) :short)
      :page-width     (/ (- right left) 1440)
      :page-height    (/ (- bottom top) 1440)
      :page-numbering (con/numbering_map (pm6/unpack input (+ 1231 offset) :char))
      :margins        (pm6/unpack input (+ offset 2222) :short 4)}}))

(defmethod parse-record 0x13 [input record]
  ;; parsing fonts
  (loop [offset   (:offset record)
         fontname ""]
    (let [c (pm6/unpack input offset :char)]
      (if (not= 0 c)
        (recur (inc offset) (str fontname (char c)))
        fontname))))


(defmethod parse-record 0x15 [input record]
  ;; parsing colors
  (let [offset      (+ 0x22 (:offset record))
        color-model (pm6/unpack input offset :unsigned-char)
        rgb         (if (= :rgb (con/color-rec-types color-model))
                      (pm6/unpack input (+ 3 offset) :unsigned-char 3)
                      (apply cmyk-to-rgb (pm6/unpack input (+ 3 offset) :short  4)))]

    {(:rec-type-name record)
     {:rgb         rgb
      :color-model (con/color-rec-types color-model)}}))


(defmethod parse-record 0x05 [input record]
  ;; parsing pages
  (let [offset   (:offset record)
        seq-num  (pm6/unpack input (+ 2 offset) :short)]
    (parse/parse-record
     input
     (first (parse/get-records-of-seq-nos input [seq-num])))))


(defmethod parse-record 0x0d [input record]
  ;;parse text
  (->>  (pm6/unpack input (:offset record) :char  (:num-recs record))
        (map char)
        clojure.string/join))


(defmethod parse-record 0x1c [input record]
  ;;parse char properties
  (let [offset                       (:offset record)
        [length font-face font-size] (pm6/unpack input  offset :short 3)
        flags                        (pm6/unpack input (+ 10 offset) :short)]
    (conj {:length         length
           :font-face      font-face
           :font-size      font-size
           :font-color     (pm6/unpack input (+ 8 offset) :short)
           :kerning        (pm6/unpack input (+ 16 offset) :short)
           :super-sub-size (pm6/unpack input (+ 20 offset) :short)
           :sub-pos        (pm6/unpack input (+ 22 offset) :short)
           :super-pos      (pm6/unpack input (+ 24 offset) :short)
           :tint           (pm6/unpack input (+ 28 offset) :short)}
          (interpret-char-flags flags))))


(defmethod parse-record 0x0b [input record]
  ;;parse paragraph properties
  (let [offset    (:offset record)
        flags     (pm6/unpack input (+ 2 offset) :char)
        keep-opts (pm6/unpack input (+ 40 offset) :short)]
    {:length         (pm6/unpack input offset :short)
     :hyphenate      (bit-and flags 0x8)
     :align          (pm6/unpack input (+ 3 offset) :char)
     :left-indent    (pm6/unpack input (+ 10 offset) :short)
     :first-indent   (pm6/unpack input (+ 12 offset) :short)
     :right-indent   (pm6/unpack input (+ 14 offset) :short)
     :before-indent  (pm6/unpack input (+ 16 offset) :short)
     :after-indent   (pm6/unpack input (+ 18 offset) :short)
     :hyphen-count   (pm6/unpack input (+ 38 offset) :char)
     :keep-together  (bit-and keep-opts 0x1)
     :keep-with-next (bit-and (bit-shift-right keep-opts 1) 0x3)
     :widows         (bit-and (bit-shift-right keep-opts 4) 0x3)
     :orphans        (bit-and (bit-shift-right keep-opts 7) 0x3)
     :rule-above     (parse-rule input (+ 44 offset))
     :rule-below     (parse-rule input (+ 62 offset))})
  )
(defmethod parse-record 0x0c [input record]
  ;;  (prn "txt styles need to be parsed")
  "txt styles needs to be parsed"
  )

(defmethod parse-record 0x1a [input record]
  ;; parsing a text block
  ;;         matcher/chanakya-to-unicode
  (let [offset          (:offset record)
        text-box-text   (pm6/unpack input (+ 4 offset) :short)
        related-records (get-records-of-seq-nos input (pm6/unpack input offset :short 6))
        parsed-records  (reduce
                         (fn [acc rec]
                           (assoc
                            acc
                            (:rec-type-name rec)
                            (parse-records input rec))) {} related-records)]
    parsed-records))

(defmethod parse-record 0x09 [input record]
  (prn "txt props need to be parsed"))

(defmethod parse-record 0x1b [input record]
  (prn "txt props b need to be parsed"))

(defmethod parse-record 0x28 [input record]
  ;;parsing an xform
  (let [offset (:offset record)]
    {:rotation-degree (pm6/unpack input offset :int)
     :skew-degree     (pm6/unpack input offset :int)
     :top-left        (parse-dims input (+ 10 offset))
     :bot-right       (parse-dims input (+ 14 offset))
     :rotating-point  (parse-dims input (+ 18 offset))
     :id              (pm6/unpack input (+ 22 offset) :int)}))

(defmethod parse-record 0x2f [input record]
  ;;parsing master
  )

(defmethod parse-record 0x31 [input record]
  ;;parsing layer
  (let [offset  (:offset record)
        layerid (pm6/unpack input (+ 42 offset) :int)]
    {:layer-id layerid}))

(defmulti parse-shape
  (fn [input record]
    (pm6/unpack input (:offset record) :char)))

(defmethod parse-shape :default [input record]
  (prn "no shape found of this id" (pm6/unpack input (:offset record) :char)))

(defmethod parse-shape 0x01 [input record]
  ;;parsing a text box shape
  (let [offset                 (:offset record)
        text-box-text-block-id (pm6/unpack input (+ 32 offset) :char)
        text-block-records     (flatten (get-expanded-records-of-type input 0x1a))
        text-block-record      (first (filter
                                       #(= text-box-text-block-id
                                           (pm6/unpack input (+ 32 (:offset %)) :int))
                                       text-block-records))]
    (prn "text block id" text-box-text-block-id)
    (prn "text block record" text-block-record)
    (parse-record
     input
     text-block-record)))

(defmethod parse-shape 0x03 [input record]
  (prn "parse line")
  ;;parse line shape
  (let [offset     (:offset record)
        mirror-var (pm6/unpack input (+ 38 offset) :short)]
    {:bbox
     {:top-left     (parse-dims input (+ 6 offset))
      :bottom-right (parse-dims input (+ 10 offset))}
     :mirrored (if (or (= mirror-var 257) (= mirror-var 0)) false true)
     :stroke-props
     {:type       (pm6/unpack input (+ 46 offset) :char)
      :width      (pm6/unpack input (+ 48 offset) :short)
      :color      (pm6/unpack input (+ 4 offset) :char)
      :over-print (pm6/unpack input (+ 58 offset) :char)
      :tint       (pm6/unpack input (+ 51 offset) :char)}}))

(defmethod parse-shape 0x04 [input record]
  ;;parse rectangle shape
  (let [offset (:offset record)]
    {:fill-props
     {:type       (pm6/unpack input (+ 38 offset) :char)
      :over-print (pm6/unpack input (+ 2 offset) :char)
      :color      (pm6/unpack input (+ 4 offset) :char)
      :tint       (pm6/unpack input (+ 224 offset) :char)}
     :bbox
     {:top-left     (parse-dims input (+ 6 offset))
      :bottom-right (parse-dims input (+ 10 offset))}
     :stroke-props
     {:type       (pm6/unpack input (+ 32 offset) :char)
      :width      (pm6/unpack input (+ 35 offset) :short)
      :color      (pm6/unpack input (+ 40 offset) :char)
      :over-print (pm6/unpack input (+ 42 offset) :char)
      :tint       (pm6/unpack input (+ 44 offset) :char)}}))

(defmethod parse-shape 0x05 [input record]
  ;;parse ellipse shape
  (let [offset (:offset record)]
    {:fill-props
     {:type       (pm6/unpack input (+ 38 offset) :char)
      :over-print (pm6/unpack input (+ 2 offset) :char)
      :color      (pm6/unpack input (+ 4 offset) :char)
      :tint       (pm6/unpack input (+ 224 offset) :char)}
     :bbox
     {:top-left     (parse-dims input (+ 6 offset))
      :bottom-right (parse-dims input (+ 10 offset))}
     :stroke-props
     {:type       (pm6/unpack input (+ 32 offset) :char)
      :width      (pm6/unpack input (+ 35 offset) :short)
      :color      (pm6/unpack input (+ 40 offset) :char)
      :over-print (pm6/unpack input (+ 42 offset) :char)
      :tint       (pm6/unpack input (+ 44 offset) :char)}}))


(defmethod parse-shape 0x06 [input record] (prn "this is a bitmap"))
(defmethod parse-shape 0x0a [input record] (prn "this is a metafile"))

(defmethod parse-shape 0x02 [input record] (prn "this is an image"))
(defmethod parse-shape 0x0e [input record] (prn "this is a group"))

(defmethod parse-shape 0x0c [input record]
  ;;parse polygon shape
  (let [offset            (:offset record)
        line-set-seq-nums (pm6/unpack input (+ 46 offset) :short)]
    {:fill-props
     {:type       (pm6/unpack input (+ 38 offset) :char)
      :over-print (pm6/unpack input (+ 2 offset) :char)
      :color      (pm6/unpack input (+ 4 offset) :char)
      :tint       (pm6/unpack input (+ 224 offset) :char)}
     :bbox
     {:top-left     (parse-dims input (+ 6 offset))
      :bottom-right (parse-dims input (+ 10 offset))}
     :points (map #(parse-dims input (:offset %)) (get-records-of-seq-nos input line-set-seq-nums))
     :closed (not= 1 (pm6/unpack input (+ 56 offset) :char))
     :stroke-props
     {:type       (pm6/unpack input (+ 32 offset) :char)
      :width      (pm6/unpack input (+ 35 offset) :short)
      :color      (pm6/unpack input (+ 40 offset) :char)
      :over-print (pm6/unpack input (+ 42 offset) :char)
      :tint       (pm6/unpack input (+ 44 offset) :char)}}))


(defmethod parse-record 0x19 [input record]
  ;;parse shapes
  (prn "parsing shapes")
  (assoc
   {:xform-id   (pm6/unpack input (+ 28 (:offset record)) :int)
    :shape-name (con/shape-record-types (pm6/unpack input (:offset record) :char))}
   :shape-data
   (parse-shape input record)))
