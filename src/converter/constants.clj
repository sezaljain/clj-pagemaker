(ns converter.constants)

(def constants
  {:toc-offset-offset 0x30
   :toc-length-offset 0x2e
   :endian-marker     (byte-array [0x99 0xff])})

(def pmd-rec-types {0x01 {:name :font-parent :size 10}
                    0x04 {:name :print-opts :size 104}
                    0x05 {:name :page :size 472}
                    0x09 {:name :txt-props :size 16}
                    0x0b {:name :para :size 80}
                    0x0c {:name :txt-styles :size 164}
                    0x0d {:name :txt :size 1}
                    0x0e {:name :tiff :size 1}
                    0x10 {:name :dont-know :size nil}
                    0x11 {:name :line-set :size 4}
                    0x13 {:name :fonts :size 94}
                    0x14 {:name :styles :size 334}
                    0x15 {:name :colors :size 210}
                    0x18 {:name :global-info :size 2496}
                    0x19 {:name :shape :size 258}
                    0x1a {:name :text-block :size 36}
                    0x1b {:name :txt-props-b :size 0x40} ;;;????? 0x40 or 0x18
                    0x1c {:name :chars :size 30}
                    0x24 {:name :img-props :size 1}
                    0x28 {:name :xform :size 26}
                    0x2f {:name :masters :size 508}
                    0x31 {:name :layers :size 46}})

(def color-rec-types {0x08 :cmyk
                      0x10 :hls
                      0x18 :rgb})

(def shape-record-types {0x01 :text
                         0x02 :image
                         0x03 :line
                         0x04 :rectangle
                         0x05 :ellipse
                         0x06 :bitmap
                         0x0a :metafile
                         0x0c :polygon
                         0x0e :group})

#_(def shape-record-types {:text      0x01
                           :line      0x03
                           :rectangle 0x04
                           :ellipse   0x05
                           :bitmap    0x06
                           :metafile  0x0a
                           :polygon   0x0c})


(def polygon-flags {0x00 :regular
                    0x01 :open
                    0x03 :closed})

(def fill-constants {:none                  0
                     :paper                 1
                     :solid                 2
                     :vertical-bars         3
                     :tight-vertical-bars   4
                     :horizontal-bars       5
                     :tight-horizontal-bars 6
                     :tilted-bars           7
                     :tight-tilted-bars     8
                     :grid-bars             9
                     :tight-grid-bars       10})

(def stroke-constants {:normal           0
                       :light-light      1
                       :dark-light       2
                       :light-dark       3
                       :light-dark-light 4
                       :dashed           5
                       :sqaure-dots      6
                       :circular-dots    7})

(def  numbering_map {0 "arabic"
                     1 "upper roman"
                     2 "lower roman"
                     3 "upper alphabetic"
                     4 "lower alphabetic"})

(def fmts {:char           1
           :signed-char    1
           :unsigned-char  1
           :bool           1
           :short          2
           :unsigned-short 2
           :int            4
           :unsigned-int   4
           :long           8
           :unsigned-long  8})

(def flags-stroke-bits{0x1 "enabled"
                       0x2 "width of text"
                       0x4 "align next para to grid"})


;; (def style-map {
;;                 0: 'single',
;;                 1: 'double',
;;                 2: 'double (upper thicker) ',
;;                 3: 'double (lower thicker) ',
;;                 4: 'triple',
;;                 5: 'dashed',
;;                 6: 'dotted (square dot)    ',
;;                 7: 'dotted (round dot)     ',
;;                 })


;; const uint8_t SHAPE_TYPE_LINE = 1;
;; const uint8_t SHAPE_TYPE_POLY = 2;
;; const uint8_t SHAPE_TYPE_RECT = 3;
;; const uint8_t SHAPE_TYPE_ELLIPSE = 4;
;; const uint8_t SHAPE_TYPE_TEXTBOX = 5;
;; const uint8_t SHAPE_TYPE_BITMAP = 6;
