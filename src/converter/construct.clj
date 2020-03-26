(ns converter.construct
  (:require [converter.matcher :refer [chanakya-to-unicode]]))

;; takes in parsed records and constructs a markdown formatted text out of it.





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

(defn markdown [txt {:keys [bold font-color small-caps font-size font-face kerning]}]
  (if (= 0 font-face)
    (chanakya-to-unicode txt)
    txt))

(defn apply-char-props [txt char-props-list]
  ;;first give all char props their respective sub-strings
  ;;font-face, font-color, font
  (prn "chars" (count char-props-list))
  (reduce
   (fn [acc v]
     (let [index      (apply + (map #(count (:txt %)) acc))
           sub-string (subs txt index (+ index (:length v)))]
       (merge acc {:txt            sub-string
                   :index          index
                   ;; :props          v
                   :formatted-text (markdown sub-string v)})))
   []
   char-props-list))

(defn apply-para-props [txt para-props-list]
  (reduce
   (fn [acc v]
     (let [index (apply + (map #(count (:txt %)) acc))]
       (merge acc {:txt   (subs txt index (+ index (:length v)))
                   :index index
                   :props v})))
   []
   para-props-list))

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

(defn debug [x] (prn (first x)) x)

(defn construct-html-text [data text-related-records]
  (let [records                  (filter-by-seq-num data text-related-records)
        {:keys [txt para chars]} (construct-text-data records)
        transformed-data         (apply-char-props txt chars)]
    {:formatted-text (->> (apply-char-props txt chars)
                          (map :formatted-text)
                          clojure.string/join)
     :txt            txt}))

(defn construct-text-block [data {:keys [parsed-data]}]
  (map #(construct-html-text data (:related-records %)) parsed-data))

(defn construct-document [data]
  (let [colors      (filter-by-rec-type data :colors)
        fonts       (filter-by-rec-type data :fonts)
        global-info (filter-by-rec-type data :global-info)
        pages       (filter-by-rec-type data :page)
        text-block  (filter-by-rec-type data :text-block)]

    (construct-text-block data (first text-block))
    #_    (map #(construct-text-block data %) text-block)))
