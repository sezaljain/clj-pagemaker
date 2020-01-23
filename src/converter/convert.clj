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


(reset! (:records file) (map parse/add-type-detail (second result)))
(swap! (:parsed-data file) merge
       {:fonts (map #(parse/parse-records file %) (parse/get-records-of-type file 0x13))})
(swap! (:parsed-data file) merge
       {:colors (map #(parse/parse-records file %) (parse/get-records-of-type file 0x15))})

(swap! (:parsed-data file) merge {:global-info
                                  (parse/parse-record
                                   file
                                   (first (parse/get-records-of-type file 0x18)))})
(swap! (:parsed-data file) merge {:pages
                                  (map
                                   #(parse/parse-records file %)
                                   (parse/get-records-of-type file 0x05))})

(parse/parse-record file (first (parse/get-records-of-type file 0x19)))

#_(swap! (:parsed-data file) merge {:shapes
                                    (map
                                     #(parse/parse-records file %)
                                     (parse/get-records-of-type file 0x19))})
                                        ;(prn (parse/add-type-detail records))

#_(parse/parse-records file (first (parse/get-records-of-type records 0x05)))
;;parse fonts, colors, xforms, global info, loop through pages
#_(parse/parse-records file (first (parse/get-records-of-type records 0x19)))
