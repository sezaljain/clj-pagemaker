(ns converter.construct
  (:require [converter.matcher :refer [chanakya-to-unicode add-symbols]]
            [clojure.string :as s]))

;; takes in parsed records and constructs a markdown formatted text out of it.

(defn debug [x] (prn x) x)

(defn filter-by-rec-type [record-data rec-type]
  (filter
   #(= rec-type
       ((if (keyword? rec-type) :rec-type-name :rec-type) %))
   record-data))

(defn filter-by-seq-num [record-data seq-num]
  (filter
   #(contains?
     (set (if seq? seq-num [seq-num]))
     (:seq %))
   record-data))

(defn remove-extra-chars [text extra-chars]
  (if (empty? extra-chars)
    text
    (remove-extra-chars
     (s/replace text (re-pattern (first extra-chars)) "")
     (rest extra-chars))))
;; :underline
;; :tint
;; :strike
;; :length
;; :outline
;; :all-caps
;; :super-sub-size
;; :shadow
;; :italic
;; :sub-pos
;; :super-pos
(defn add-tag
  ([text tag props]
   (let [tag-start (s/replace tag #">" (s/join [(reduce (fn [acc [k v]] (str " " k "=" v " ")) "" props) ">"]))
         tag-close (s/replace tag #"<" "</")]
     (str tag-start text tag-close)))
  ([text tag]
   (let [tag-close (s/replace tag #"<" "</")]
     (str tag text tag-close))))

(defn add-tag- [txt tag]
  (if (= "-" (s/trim txt))
    txt
    (str tag (s/trim txt) tag " ")))

(defn add-heading [txt font-size font]
  (cond
    (and (= font "Chanakya") (= 220 font-size))        (str "\n# " txt)
    (and (= font "Times New Roman") (= 180 font-size)) (str "\n# " txt)
    (and (= font "Chanakya") (= 180 font-size))        (str "\n## " txt)
    (and (= font "Chanakya") (= 160 font-size))        (str "\n### " txt)
    :else                                              txt))

(defn markdown [txt {:keys [font font-face font-color font-size bold italic sub super underline]}]
  (if (= 0 (count (s/trim txt)))
    txt
    (cond-> txt
      (= "Chanakya" font)    (chanakya-to-unicode)
      (= "Wingdings 2" font) (add-symbols)
      (= 1 bold)             (add-tag- "**")
      (= 1 italic)           (add-tag- "_")
      (= 1 sub)              (add-tag "<sub>")
      (= 1 super)            (add-tag "<sup>")
      (= 1 underline)        (add-tag "<u>")
      font-size              (add-heading font-size font)
      #_                     (add-tag "<font>" {"size" (Math/floor (/ font-size 25))})
      :always                (remove-extra-chars ["" "" "" "" "" "" "" "\\*\\*\\*\\*"]))))

#_(defn add-markdown-table
    ([txt] (add-markdown-table txt "="))
    ([txt column-separator]
     (if (> (count (re-seq (re-pattern column-separator) txt)) 1)
       (s/replace txt (re-pattern column-separator) "|")
       txt)))

(defn add-markdown-table [txt]
  (s/join "|" (map s/trim (s/split txt  #"="))))

(defn para-format [txt {:keys [align left-indent after-indent before-indent right-indent first-indent rule-below rule-above orphans widows keep-with-next hyphen-count hyphenate length] :as v}]
  (if (= 0 (count txt))
    txt
    (cond-> txt
      #_      (> (count (re-seq #"=" txt)) 1) #_ (add-markdown-table)
      #_      (= 2 align)                     #_ (add-tag "<center>")
      :always (str "\n  "))))


(defn construct-text-data [text-related-records]
  (let [grouped-records (->> text-related-records
                             (map #(select-keys % [:seq :rec-type-name :parsed-data]))
                             (group-by :rec-type-name))]
    (reduce
     (fn [acc [k v]]
       (let [grouped-data (map :parsed-data v)]
         (assoc
          acc k
          (if (= :txt k)
            (clojure.string/join grouped-data)
            (flatten grouped-data)))))
     {} grouped-records)))

(defn apply-char-props [txt char-props-list offset]
  ;;apply char props to a signle para text
  ;;we have txt distributed into paras at this point
  ;; every para has start index and sub string
  (reduce
   ;;this reduce R1 loops over relevant chars and applies markdown based on char props to txt
   (fn [acc v]
     (let [start (- (max (:start v) offset) offset)
           end   (- (min (:end v) (+ offset (count txt))) offset)]
       (clojure.string/join [acc (markdown (subs txt start end) v)])))
   ""
   char-props-list))

(defn apply-para-char-props [txt para-props-list char-props-list]
  (reduce
   (fn [acc para]
     (let [relevant-chars (filter #(and (< (:start %) (:end para)) (> (:end %) (:start para))) char-props-list)
           sub-txt        (subs txt (:start para) (:end para))]
       (merge acc {:txt (-> sub-txt
                            (apply-char-props relevant-chars (:start para))
                            (para-format para))})))
   []
   para-props-list))


(defn remove-duplicates [prop-list]
  #_  (prn prop-list "}}}}}")
  (reduce
   (fn [acc v]
     (let [last-v  (last acc)
           is-same (= (dissoc last-v :length) (dissoc v :length))]
       #_(if is-same (prn "found same " v)
             (prn last-v v))
       (if is-same
         (assoc acc (dec (count acc)) (assoc last-v :length (+ (:length last-v) (:length v))))
         (merge acc v))))
   []
   prop-list))

(defn add-start-end [prop-list]
  (reductions
   (fn [acc v]
     (let [start (or (:end acc) (:length acc))]
       (assoc v :start start :end (+ start (:length v)))))
   (assoc (first prop-list) :start 0 :end (:length (first prop-list)))
   (rest prop-list)))

(defn construct-html-text [data fonts text-related-records]
  (let [records                  (filter-by-seq-num data text-related-records)
        {:keys [txt para chars]} (construct-text-data records)
        txt                      (or txt "")
        chars-with-start-end     (map #(assoc % :font (nth fonts (:font-face %))) (add-start-end chars))
        para-with-start-end      (add-start-end para)
        transformed-data         (apply-para-char-props  txt  para-with-start-end chars-with-start-end)]
    #_(prn transformed-data "=====================")
    {:formatted-text (clojure.string/join (map :txt transformed-data))
     :txt            txt}))

(defn construct-text-block [data fonts parsed-data]
  ;;  (prn parsed-data)
  (if (seq? parsed-data)
    (map #(construct-text-block data fonts %) parsed-data)
    (construct-html-text data fonts (:related-records parsed-data))))

(defn construct-document [data]
  (let [colors      (map :parsed-data (filter-by-rec-type data :colors))
        fonts       (flatten (map :parsed-data (filter-by-rec-type data :fonts)))
        global-info (map :parsed-data (filter-by-rec-type data :global-info))
        pages       (map :parsed-data (filter-by-rec-type data :page))
        text-block  (map :parsed-data (filter-by-rec-type data :text-block))]
    (map #(construct-text-block data fonts %) (first text-block))
    #_    (map #(construct-text-block data %) text-block)))
