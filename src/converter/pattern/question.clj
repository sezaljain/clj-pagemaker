(ns converter.pattern.question
  (:require [clojure.string :as s]
            [clojure.string :as str]))

;;find questions in a certain txt
;; and tag them accordingly

(defn add-tag [txt tag]
  (let [tag-close (s/replace tag #"<" "</")]
    (str tag txt tag-close)))

(defn is-valid-question [ques]
  (if (empty? ques)
    false
    (and (= (nth ques 1) "(1)")
         (= (nth ques 3) "(2)")
         (= (nth ques 5) "(3)")
         (= (nth ques 7) "(4)"))))

(defn question-markdown [ques]
  (str (:question-text ques)
       (add-tag
        (s/join (mapv #(add-tag % "<li>") (vals (:options ques))))
        "<ol>")
       (when (:answer ques) (str "\n Answer: " (:answer ques)))
       (when (:hint ques) (str "\n " (:hint ques)))
       "\n\n ---\n"))

(defn question-json [ques]
  {:question-text (nth ques 0)
   :options       {1 (nth ques 2)
                   2 (nth ques 4)
                   3 (nth ques 6)
                   4 (nth ques 8)}
   :answer        (Integer/parseInt (re-find #"\d"(nth ques 9)))
   :hint          (nth ques 10 nil)})

(defn debug [x] (prn x) x)

(defn form-question-groups [text]
  (->> (-> text
           (s/split #"(?<=\(\d\))|(?=\(\d\))|(?=◦)|(?<=◦)"))
       (map  #(s/trim-newline (s/trim %)))
       (remove empty?)
       (reduce (fn [acc v]
                 (let [last-q       (last acc)
                       last-q-index (dec (count acc))
                       q-start      (= v "◦")]
                   (if q-start
                     (merge acc [])
                     (if (vector? last-q)
                       (assoc acc last-q-index (merge last-q v))
                       acc)))) [])
       (filter is-valid-question)
       (map question-json)
       (map question-markdown)))

#_(def questions (s/join (form-question-groups txt)))


(defn get-questions [text]
  (s/join (form-question-groups text)))
