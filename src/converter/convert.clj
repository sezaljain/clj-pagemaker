(ns converter.convert
  (:require [converter.records :as rec]
            [converter.pagemaker-buffer :as pm6]
            [converter.parse :as parse]
            [converter.matcher :as matcher]
            [converter.construct :as construct]))


(def file {:filename    "../Sezal.p65"
           :buffer      (pm6/open-gsf "../Sezal.p65")
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

(def dd (map :formatted-text text-data))
(prn  "--------------------------")
(prn dd)
(prn "--------------------------------------------")
;;(def ddd (reduce (fn [acc v] (clojure.string/join "<br>" [acc  (:formatted-text v)])) "" text-data))
(prn (clojure.string/join dd))
