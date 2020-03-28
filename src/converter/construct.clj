(ns converter.construct
  (:require [converter.matcher :refer [chanakya-to-unicode]]
            [clojure.string :as s]))

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

(defn add-tag [text tag]
  ;;; assumes tag is <sdfasfasf>
  (let [tag-close (s/replace tag #"<" "</")]
    (str tag text tag-close)))

#_(defn remove-adjoining-tags [text tags]
    (reduce
     (fn [acc tag]
       (let [pattern (str (s/replace tag #"<" "</") tag)]
         (prn pattern)
         (s/replace text (re-pattern pattern) "")))
     text tags))

(defn markdown [txt {:keys [font-face font-color font-size bold italic sub super underline]}]
  (cond-> txt
    (= 0 font-face) (chanakya-to-unicode)
    (= 1 bold)      (add-tag "<b>")
    (= 1 italic)    (add-tag "<i>")
    (= 1 sub)       (add-tag "<sub>")
    (= 1 super)     (add-tag "<sup>")
    (= 1 underline) (add-tag "<u>")
    ;;   :always         (remove-adjoining-tags ["<b>" "<i>" "<sub>" "<sup>" "<u>"])
    ))

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

(defn apply-char-props [txt-paras char-props-list]
  ;;we have txt distributed into paras at this point
  ;; every para has start index and sub string\
  (let [chars-with-start-end (reductions
                              (fn [acc v]
                                (let [start (or (:end acc) (:length acc))]
                                  (assoc v :start start :end (+ start (:length v)))))
                              (assoc (first char-props-list) :start 0 :end (:length (first char-props-list)))
                              (rest char-props-list))]
    (map
     ;;this first map M1 runs a loop over all para props with fn A
     (fn [para]
       ;; this is fn A, first we find all relevant char props for a particular para (based on start,end)
       (let [relevant-chars (filter #(and (< (:start %) (:end para)) (> (:end %) (:start para))) chars-with-start-end)]
         {:char-text (add-tag
                      (reduce
                       ;;this reduce R1 loops over relevant chars and applies markdown based on char props to txt
                       (fn [acc v]
                         (let [start (- (max (:start v) (:start para)) (:start para))
                               end   (- (min (:end v) (:end para)) (:start para))]
                           (clojure.string/join [acc (markdown (subs (:txt para) start end) v)])))
                       ""
                       relevant-chars)
                      "<p>")} ))
     txt-paras)))

(defn apply-para-props [txt para-props-list]
  (reduce
   (fn [acc v]
     (let [start (or (:end (last acc)) 0)
           end   (+ start (:length v))]
       (merge acc {:txt   (subs txt start end)
                   :start start
                   :end   end})))
   []
   para-props-list))


(defn debug [x] (prn (first x)) x)

(defn construct-html-text [data text-related-records]
  (let [records                  (filter-by-seq-num data text-related-records)
        {:keys [txt para chars]} (construct-text-data records)
        transformed-data         (-> (apply-para-props txt para)
                                     (apply-char-props chars))]
    {:formatted-text (clojure.string/join (map :char-text transformed-data))
     :txt            txt}))

(defn construct-text-block [data {:keys [parsed-data]}]
  (prn parsed-data)
  (map #(construct-html-text data (:related-records %)) parsed-data))

(defn construct-document [data]
  (let [colors      (filter-by-rec-type data :colors)
        fonts       (filter-by-rec-type data :fonts)
        global-info (filter-by-rec-type data :global-info)
        pages       (filter-by-rec-type data :page)
        text-block  (filter-by-rec-type data :text-block)]
    (construct-text-block data (first text-block))
    #_    (map #(construct-text-block data %) text-block)))


;;first distribute text into para strings
