(ns converter.convert
  (:require [converter.records :as rec]
            [converter.pagemaker-buffer :as pm6]
            [converter.parse :as parse]
            [converter.matcher :as matcher]
            [converter.construct :as construct]
            [converter.pattern.question :as question]))


(def file {:filename    "../A.p65"
           :buffer      (pm6/open-gsf "../A.p65")
           :endian      (atom :little)
           :header      (atom {})
           :offset      (atom 0)
           :seq-no      (atom 0)
           :records     (atom [])
           :parsed-data (atom {})
           :markdown    (atom "")})


(rec/parse-header file)

(def result
  (rec/read-next-record-toc
   file
   (:toc-offset @(:header file))
   (:toc-length @(:header file))
   0
   false
   0))


(reset! (:records file) (map parse/add-type-detail (flatten (second result))))
(def parsed (map #(assoc % :parsed-data (parse/parse-records file %)) @(:records file)))
(def text-data  (construct/construct-document parsed))
#_(def text {:txt            (map :txt text-data)
             :formatted-text (clojure.string/join (map :formatted-text text-data))})

(def formatted (map question/get-questions (mapv :formatted-text text-data)))
(def dd (map :formatted-text text-data))
(prn  "--------------------------")

(prn "--------------------------------------------")
;;(def ddd (reduce (fn [acc v] (clojure.string/join "<br>" [acc  (:formatted-text v)])) "" text-data))
(print (clojure.string/join dd))
0
1
(print (clojure.string/join formatted))
