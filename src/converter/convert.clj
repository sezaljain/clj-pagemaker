(ns converter.convert
  (:require [converter.records :as rec]
            [converter.pagemaker-buffer :as pm6]
            [converter.parse :as parse]))


(def file {:filename    "../Sezal.p65"
           :buffer      (pm6/open-gsf "../Sezal.p65")
           :endian      (atom :little)
           :header      (atom {})
           :offset      (atom 0)
           :seq-no      (atom 0)
           :records     (atom [])
           :parsed-data (atom {})})


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
#_(swap! (:parsed-data file) merge
         {:fonts (map #(parse/parse-records file %) (parse/get-records-of-type file 0x13))})
#_(swap! (:parsed-data file) merge
         {:colors (map #(parse/parse-records file %) (parse/get-records-of-type file 0x15))})
;;parse xforms?
#_(swap! (:parsed-data file) merge
         {:xforms (map #(parse/parse-records file %) (parse/get-records-of-type file 0x28))})

#_(swap! (:parsed-data file) merge {:global-info
                                    (parse/parse-record
                                     file
                                     (first (parse/get-records-of-type file 0x18)))})
#_(swap! (:parsed-data file) merge {:pages
                                    (map
                                     #(parse/parse-records file %)
                                     (parse/get-records-of-type file 0x05))})
(prn  "--------------------------")
#_(swap!
   (:parsed-data file)
   merge
   {:shapes (map #(parse/parse-records file %) (parse/get-records-of-type file 0x19))})
(prn "---------------------------")


#_(prn (map #(parse/parse-record file %) (parse/get-records-of-seq-nos file [61 62 63 64 65 66 67])))

#_(swap! (:parsed-data file) merge {:shapes
                                    (map
                                     #(parse/parse-records file %)
                                     (parse/get-records-of-type file 0x19))})
                                        ;(prn (parse/add-type-detail records))

#_(parse/parse-records file (first (parse/get-records-of-type records 0x05)))
;;parse fonts, colors, xforms, global info, loop through pages
#_(parse/parse-records file (first (parse/get-records-of-type records 0x19)))
