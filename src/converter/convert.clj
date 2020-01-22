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

(def records; (second result)
  (map parse/add-type-detail (second result))
  )
                                        ;(prn (parse/add-type-detail records))
(def file-info
  {:header      @(:header file)
   :fonts       (parse/parse-fonts file records)
   :colors      (parse/parse-colors file records)
   :global-info (parse/parse-record
                 file
                 (first (parse/get-records-of-type records 0x18)))})

(prn file-info)

;;parse fonts, colors, xforms, global info, loop through pages
