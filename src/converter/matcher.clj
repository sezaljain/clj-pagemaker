(ns converter.matcher
  (:require [clojure.string :as s]))

(def unicode-string "जनता द्वारा निर्वाचित सरकार की नीतियों एवं योजनाओं को क्रियान्वित करने का दायित्व प्रशासन तंत्र का होता है। राज्य के प्रशासन तंत्र के शीर्ष स्तर पर शासन सचिवालय है जो जयपुर में स्थित है। शासन सचिवालय समस्त राज्य के प्रशासन का नियंत्रक एवं केंद्रीय बिंदु है। यह संविधान की राज्य सूची में उल्लेखित विषयों के प्रशासन हेतु उत्तरदायी होता है। शासन सचिवालय राज्य मंत्रिमंडल को सरकार की नीतियों के निर्धारण, निर्णयों एवं उनके क्रियान्वयन की प्रगति का अवलोकन एवं मूल्यांकन करने में आवश्यक सलाह एवं सहायता प्रदान करता है। साथ ही यह मंत्रिपरिषद द्वारा बनाई गई नीतियों, नियमों एवं लिए गए निर्णयों को लागू करवाने का कार्य करता है। राज्य प्रशासन को कार्य संचालन की दृष्टि से विभिन्न विभागों में  बाँटा गया है। प्रत्येक विभाग का राजनैतिक मुखिया प्रभारी मंत्री होता है। उसकी सहायता के लिए राज्यमंत्री एवं उपमंत्री होते हैं। विभाग के पर्यवेक्षण, निर्देशन एवं नियंत्रण का कार्य प्रमुख शासन सचिव का होता है। शासन सचिव प्रभारी मंत्री एवं विभागीय प्रशासन के मध्य कड़ी का कार्य करता है। वह प्रभारी मंत्री को उचित परामर्श प्रदान करता है एवं सरकारी निर्णयों को अधीनस्थ विभाग द्वारा क्रियान्वित करवाता है। ")
(def chanakya-key {:move ["æ" "ð" "ि"]
                   ;;   "ै"  1

                   :symbols            [["Ï" "।"]]
                   :matra              [["åï" "ो"]
                                        ["ó" "ो"]
                                        ["ô" "ौ"]
                                        ["å" "ा"]
                                        ["æ" "ि"]
                                        ["ç" "ी"]
                                        ["è" "ु" ]
                                        ["é" "ू"]
                                        ["ï" "े"]
                                        ["ð" "ै"]
                                        ["¡" "ं"]
                                        ["³" "ं"]
                                        [" " "ँ"]
                                        ["ñ" "ॅ"]
                                        ["ê" "ृ"]
                                        ["Ð" ":"]
                                        ["ö" "्"]
                                        ;;missing stuff  "ृ"  "ॅ"  "ौ"
                                        ["Z" "û³"]]
                   :cleanup            [["्ा" ""]
                                        ["्ो" "े"]
                                        ["अा" "आ"]
                                        ["आै" "औ"]
                                        ["एे" "ऐ"]]
                   :double-letter-keys [["ÙU" "र"]
                                        ["âU" "ह"]
                                        ["ÄU" "ट"]
                                        ["¦û" "ई"]
                                        ["¨U" "उ"]
                                        ["Ç£" "फ"]
                                        ["ÆU" "ड"]
                                        ["Æ·" "ड़"]
                                        ["ÅU" "ठ"]
                                        ["îU" "ठ"]
                                        ["¶£" "क"]
                                        ["ÈU" "ढ"]
                                        ["¿U" "छ"]
                                        ["M£" "रु"]
                                        ["Uâ" "ह"]
                                        ["mU" "द्व"]
                                        ["Q£" "¶£öË"]]
                   :triple-letter-keys [["¤åï" "ओ"]
                                        ["Æ·U" "ड़"]
                                        ]
                   :single-letter-keys [["¤" "अ"]
                                        ["¯" "ए"]
                                        ["¦" "इ"]
                                        ["À" "ज"]
                                        ["Á" "प"]
                                        ["Þ" "व"]
                                        ["Ô" "ब"]
                                        ["Õ" "भ"]
                                        ["Ë" "त"]
                                        ["á" "स"]
                                        ["à" "ष"]
                                        ["Í" "द"]
                                        ["Î" "ध"]
                                        ["Ì" "थ"]
                                        ["Ö" "म"]
                                        ["â" "ह"]
                                        ["¾" "च"]
                                        ["Û" "ल"]
                                        ["º" "ग"]
                                        ["¸" "ख"]
                                        ["Ø" "न"]
                                        ["Ù" "र"]
                                        ["×" "य"]
                                        ["S" "स्"]
                                        ["»" "द"]
                                        ["c" "ष्"]
                                        ["D" "ष्"]
                                        ["" "घ्"]
                                        ["" "ण्"]
                                        ["" "ज्"]
                                        ["" "ज्"]
                                        ["" "त्"]
                                        ["" "न्"]
                                        ["" "म्"]
                                        ["" "ध्"]
                                        ["" "ख्"]
                                        ["" "च्"]
                                        ["" "श्र्"]
                                        [""  "ग्"]
                                        ["þ" "क्ष्"]
                                        ["Ê" "ल्"]
                                        ["Â" "व्"]
                                        ["ß" "श्"]
                                        ["Ú" "झ्"]
                                        ["\230" "ज्ञ्"]
                                        ["m" "द्व"]
                                        ["" "त्र्"]
                                        ["" "त्त्"]
                                        ["" "ल्ल"]
                                        ["ò" "न्न्"]]
                   :numbers            [["­" "0"]
                                        ["v" "1"]
                                        ["w" "2"]
                                        ["x" "3"]
                                        ["y" "4"]
                                        ["z" "5"]
                                        ["\\{" "6"]
                                        ["\\|" "7"]
                                        ["}" "8"]
                                        ["~" "9"]
                                        ]
                   :order              [:triple-letter-keys :double-letter-keys :single-letter-keys :matra :numbers :cleanup :symbols
                                        ]})

(def unicode-sentences
  ["जनता द्वारा निर्वाचित सरकार की नीतियों एवं योजनाओं को क्रियान्वित करने का दायित्व प्रशासन तंत्र का होता है।"
   "राज्य के प्रशासन तंत्र के शीर्ष स्तर पर शासन सचिवालय है जो जयपुर में स्थित है।"
   "शासन सचिवालय समस्त राज्य के प्रशासन का नियंत्रक एवं केंद्रीय बिंदु है।"
   "यह संविधान की राज्य सूची में उल्लेखित विषयों के प्रशासन हेतु उत्तरदायी होता है।"
   "शासन सचिवालय राज्य मंत्रिमंडल को सरकार की नीतियों के निर्धारण, निर्णयों एवं उनके क्रियान्वयन की प्रगति का अवलोकन एवं मूल्यांकन करने में आवश्यक सलाह एवं सहायता प्रदान करता है।"
   "साथ ही यह मंत्रिपरिषद द्वारा बनाई गई नीतियों, नियमों एवं लिए गए निर्णयों को लागू करवाने का कार्य करता है। राज्य प्रशासन को कार्य संचालन की दृष्टि से विभिन्न विभागों में  बाँटा गया है।"
   "प्रत्येक विभाग का राजनैतिक मुखिया प्रभारी मंत्री होता है।"
   "उसकी सहायता के लिए राज्यमंत्री एवं उपमंत्री होते हैं।"
   "विभाग के पर्यवेक्षण, निर्देशन एवं नियंत्रण का कार्य प्रमुख शासन सचिव का होता है।"
   "शासन सचिव प्रभारी मंत्री एवं विभागीय प्रशासन के मध्य कड़ी का कार्य करता है।"
   "वह प्रभारी मंत्री को उचित परामर्श प्रदान करता है एवं सरकारी निर्णयों को अधीनस्थ विभाग द्वारा क्रियान्वित करवाता है। "])
(def chanakya-sentences
  ["ÀØËå mUåÙUå æØÞåûæ¾Ë áÙU¶£åÙU ¶£ç ØçæË×åï³ ¯Þ³ ×åïÀØå¤åï³ ¶£åï æ¶ü£×åæÞË ¶£ÙUØï ¶£å Íåæ×Þ ÁüßååáØ Ë³å ¶£å âUåïËå âðUÏ"
   "ÙUå× ¶ï£ ÁüßååáØ Ë¡å ¶ï£ ßåçàû SËÙU ÁÙU ßååáØ áæ¾ÞåÛ× âðU Àó À×ÁèÙU Öï³ æSÌË âðUÏ"
   "ßååáØ áæ¾ÞåÛ× áÖSË ÙUå× ¶ï£ ÁüßååáØ ¶£å æØ×¡å¶£ ¯Þ¡ ¶ï£»ü æÔÍè âðUÏ"
   "×âU á³æÞååØ ¶£ç ÙUå× áé¾ç Ö³ï ¨Uïæ¸Ë æÞà×åï³ ¶ï£ ÁüßååáØ âïUËè ¨UåÙUÍå×ç âUåïËå âðUÏ"
   "ßååáØ áæ¾ÞåÛ× ÙUå× Ö³æåÖ³ÆUÛ ¶£åï áÙU¶£åÙU ¶£ç ØçæË×åï³ ¶ï£ æØååûÙUå, æØåû×åï³ ¯Þ³ ¨UØ¶ï£ æ¶ü£×åÞ×Ø ¶£ç ÁüºæË ¶£å ¤ÞÛåï¶£Ø ¯Þ³ ÖéÊ×å³¶£Ø ¶£ÙUØï Ö³ï ¤åÞß×¶£ áÛåâU ¯Þ³ áâUå×Ëå ÁüÍåØ ¶£ÙUËå âðUÏ"
   "áåÌ âUç ×âU Ö¡æåÁæÙUàÍ måÙUå ÔØå¦û º¦û ØçæË×ó³, æØ×Öó³ ¯Þ¡ æÛ¯ º¯ æØåû×ó³ ¶£ó Ûåºé ¶£ÙUÞåØï ¶£å ¶£å×û ¶£ÙUËå âðUÏ"
   "\rÙUå× ÁüßååáØ ¶£ó ¶£å×û á¡¾åÛØ ¶£ç ÍêæCîU áï æÞæÕòå æÞÕåºó³ Öï³ Ôå ÄUå º×å âðUÏ"
   "Áü×ï¶£ æÞÕåº ¶£å ÙUåÀØðæË¶£ Öèæ¸×å ÁüÕåÙUç Ö¡åç âUóËå âðUÏ"
   "¨Uá¶£ç áâUå×Ëå ¶ï£ æÛ¯ ÙUå×Ö¡åç ¯Þ¡ ¨UÁÖ¡åç âUóËï âð¡UÏ"
   "æÞÕåº ¶ï£ Á×ûÞïþåå, æØÍïûßåØ ¯Þ¡ æØ×¡åå ¶£å ¶£å×û ÁüÖè¸ ßååáØ áæ¾Þ ¶£å âUóËå âðU Àó æÞÕåº ¶£å ÁüßååáæØ¶£ Þ ¶£å×û¶£åÙUç Öèæ¸×å âUóËå âðUÏ"
   "ßååáØ áæ¾Þ ÁüÕåÙUç Ö¡åç ¯Þ¡ æÞÕåºç× ÁüßååáØ ¶ï£ Ö× ¶£Æ·Uç ¶£å ¶£å×û ¶£ÙUËå âðUÏ"
   "ÞâU ÁüÕåÙUç Ö¡åç ¶£ó ¨Uæ¾Ë ÁÙUåÖßåû ÁüÍåØ ¶£ÙUËå âðU ¯Þ¡ áÙU¶£åÙUç æØåû×ó³ ¶£ó ¤ÎçØSÌ æÞÕåº måÙUå æ¶ü£×åæÞË ¶£ÙUÞåËå âðUÏ"])


(def chanakya-string "ÀØËå mUåÙUå æØÞåûæ¾Ë áÙU¶£åÙU ¶£ç ØçæË×åï³ ¯Þ³ ×åïÀØå¤åï³ ¶£åï æ¶ü£×åæÞË ¶£ÙUØï ¶£å Íåæ×Þ ÁüßååáØ Ë³å ¶£å âUåïËå âðUÏ ÙUå× ¶ï£ ÁüßååáØ Ë¡å ¶ï£ ßåçàû SËÙU ÁÙU ßååáØ áæ¾ÞåÛ× âðU Àó À×ÁèÙU Öï³ æSÌË âðUÏ ßååáØ áæ¾ÞåÛ× áÖSË ÙUå× ¶ï£ ÁüßååáØ ¶£å æØ×¡å¶£ ¯Þ¡ ¶ï£»ü æÔÍè âðUÏ ×âU á³æÞååØ ¶£ç ÙUå× áé¾ç Ö³ï ¨Uïæ¸Ë æÞà×åï³ ¶ï£ ÁüßååáØ âïUËè ¨UåÙUÍå×ç âUåïËå âðUÏ ßååáØ áæ¾ÞåÛ× ÙUå× Ö³æåÖ³ÆUÛ ¶£åï áÙU¶£åÙU ¶£ç ØçæË×åï³ ¶ï£ æØååûÙUå, æØåû×åï³ ¯Þ³ ¨UØ¶ï£ æ¶ü£×åÞ×Ø ¶£ç ÁüºæË ¶£å ¤ÞÛåï¶£Ø ¯Þ³ ÖéÊ×å³¶£Ø ¶£ÙUØï Ö³ï ¤åÞß×¶£ áÛåâU ¯Þ³ áâUå×Ëå ÁüÍåØ ¶£ÙUËå âðUÏ áåÌ âUç ×âU Ö¡æåÁæÙUàÍ måÙUå ÔØå¦û º¦û ØçæË×ó³, æØ×Öó³ ¯Þ¡ æÛ¯ º¯ æØåû×ó³ ¶£ó Ûåºé ¶£ÙUÞåØï ¶£å ¶£å×û ¶£ÙUËå âðUÏ \rÙUå× ÁüßååáØ ¶£ó ¶£å×û á¡¾åÛØ ¶£ç ÍêæCîU áï æÞæÕòå æÞÕåºó³ Öï³ Ôå ÄUå º×å âðUÏ Áü×ï¶£ æÞÕåº ¶£å ÙUåÀØðæË¶£ Öèæ¸×å ÁüÕåÙUç Ö¡åç âUóËå âðUÏ ¨Uá¶£ç áâUå×Ëå ¶ï£ æÛ¯ ÙUå×Ö¡åç ¯Þ¡ ¨UÁÖ¡åç âUóËï âð¡UÏ æÞÕåº ¶ï£ Á×ûÞïþåå, æØÍïûßåØ ¯Þ¡ æØ×¡åå ¶£å ¶£å×û ÁüÖè¸ ßååáØ áæ¾Þ ¶£å âUóËå âðU Àó æÞÕåº ¶£å ÁüßååáæØ¶£ Þ ¶£å×û¶£åÙUç Öèæ¸×å âUóËå âðUÏ ßååáØ áæ¾Þ ÁüÕåÙUç Ö¡åç ¯Þ¡ æÞÕåºç× ÁüßååáØ ¶ï£ Ö× ¶£Æ·Uç ¶£å ¶£å×û ¶£ÙUËå âðUÏ ÞâU ÁüÕåÙUç Ö¡åç ¶£ó ¨Uæ¾Ë ÁÙUåÖßåû ÁüÍåØ ¶£ÙUËå âðU ¯Þ¡ áÙU¶£åÙUç æØåû×ó³ ¶£ó ¤ÎçØSÌ æÞÕåº måÙUå æ¶ü£×åæÞË ¶£ÙUÞåËå âðUÏ")


(def unicode (clojure.string/split unicode-string #" "))
(def chanakya (clojure.string/split chanakya-string #" "))

(defn replace-values [text keys-to-replace]
  (if (empty? keys-to-replace)
    text
    (let [remaining-keys (rest keys-to-replace)
          [key value]    (first keys-to-replace)]
      ;;    (when (s/index-of text key) (prn "- " key value text))
      (replace-values
       (s/replace text (re-pattern key) value)
       (rest keys-to-replace)))))

(defn get-keys-in-order
  ([cipher-key] (get-keys-in-order cipher-key (:order cipher-key)))
  ([cipher-key order]
   (reduce
    (fn [acc k]
      (concat acc (k cipher-key)))
    []
    order)))

(defn replace-values-in-order [text cipher-key]
  (replace-values text (get-keys-in-order cipher-key)))

(defn transform-double-letter-keys [text double-letter-consonants]
  (reduce
   (fn [acc [c _]]
     (let [[v1 v2]  (s/split c #"")
           pattern1 (re-pattern (str v1 "." v2))
           pattern2 (re-pattern (str v1 ".." v2))]
       ;;       (prn "===" c v1 v2  acc)
       (s/replace
        (s/replace
         acc
         pattern1
         #(s/join [v1 v2 (subs %1 1 2)]))
        pattern2
        #(s/join [v1 v2 (subs %1 1 3)]))))
   text
   double-letter-consonants))

#_(defn move-values [text keys-to-move]
    (reduce
     (fn [acc [k v]]
       (let [pattern (if (= 1 v) (str k ".") (str "." k))]
         (s/replace
          acc
          (re-pattern pattern)
          #(s/join
            (if (= 1 v)
              [(last %1) k]
              [k (first %1)])))))
     text
     keys-to-move))

(defn move-one-key [full-string curr-index key-to-move all-keys matras]
  (let [pos-   (s/index-of (subs full-string curr-index) key-to-move)
        length (count full-string)]
    (if (and pos- (not= (+ curr-index pos-) (dec length)))
      (let [pos             (+ pos- curr-index)
            next-char       (first (filter
                                    #(= % (subs
                                           full-string
                                           (inc pos)
                                           (min length (+ 1 pos (count %)))))
                                    (flatten all-keys)))
            new-index-v1    (min length (+ pos (count key-to-move) (count next-char)))
            ;;  _               (prn new-index-v1 length next-char)
            after-next-char (subs full-string new-index-v1 (min length (inc new-index-v1)))
            is-matra        (not= -1 (.indexOf matras after-next-char))
            new-index       (+ new-index-v1 (if is-matra (+ 1 (count after-next-char)) 0))
            updated-string  (str
                             (subs full-string 0 pos) next-char
                             (subs full-string new-index-v1 new-index)
                             key-to-move
                             (subs full-string new-index))]
        (move-one-key
         updated-string
         new-index
         key-to-move
         all-keys
         matras))
      full-string)))

(defn move-keys [full-string cipher-key]
  (let [keys-to-move (:move cipher-key)
        all-keys     (get-keys-in-order cipher-key (disj (set (:order cipher-key)) :symbols :cleanup))
        matras       (flatten (:matra cipher-key))]
    (reduce
     (fn [acc v]
       (move-one-key acc 0 v all-keys matras))
     full-string
     keys-to-move)))

(defn add-half-r-1
  "half r position is moved based on prev character"
  [full-string curr-index]
  (let [matras     ["ा" "ि" "ी" "ु" "ू" "ृ" "े" "ै" "ो" "ौ" "ं" ":" "ँ" "ॅ"]
        half-r-pos (s/index-of (subs full-string curr-index) "û")]
    (if half-r-pos
      (let [p              (+ curr-index half-r-pos)
            check-if-matra (not= -1 (.indexOf matras (subs full-string (dec p) p)))
            start-pos      (cond
                             (= true check-if-matra) (- p 2)
                             :default                (- p 1))
            character      (subs full-string (dec p) p)]
        (add-half-r-1
         (str
          (subs full-string 0 start-pos)
          "र्"
          (subs full-string start-pos p)
          (subs full-string (inc p)))
         (inc p)))
      full-string)))

(defn add-half-r-2
  "half r position is moved based on prev character प्रभारी"
  [full-string curr-index]
  (let [matras     ["ा" "ि" "ी" "ु" "ू" "ृ" "े" "ै" "ो" "ौ" "ं" ":" "ँ" "ॅ"]
        half-r-pos (s/index-of (subs full-string curr-index) "ü")]
    (if half-r-pos
      (let [p              (+ curr-index half-r-pos)
            check-if-matra (not= -1 (.indexOf matras (subs full-string (dec p) p)))
            start-pos      (if check-if-matra (- p 1) p)
            character      (subs full-string (dec p) p)]
        (add-half-r-2
         (str
          (subs full-string 0 start-pos)
          "्र"
          (subs full-string start-pos p)
          (subs full-string (inc p)))
         (inc p)))
      full-string)))

(defn debug [x debug?] (when (true? debug?) (prn "?" x))x)

(defn decipher-string
  ([text cipher-key] (decipher-string text cipher-key false))
  ([text cipher-key debug?]
   (-> text
       (transform-double-letter-keys (:double-letter-keys cipher-key))
       (debug debug?)
       (replace-values-in-order cipher-key)
       (debug debug?)
       (move-keys  cipher-key)
       (debug debug?)
       (add-half-r-1 0)
       (add-half-r-2 0)
       (replace-values-in-order  cipher-key))))



#_(defn get-substrings [s substr-size]
    (if (vector? s)
      (flatten (map #(get-substrings % substr-size) s))
      (map #(subs s % (+ substr-size %)) (range (- (count s) (dec substr-size))))))

#_(defn remove-low-freq [freq-map min-freq]
    (filter #(> (last %) min-freq) freq-map))

#_(defn create-initial-key [u c]
    (let [unicode-monogram  (reverse
                             (sort-by val (frequencies (get-substrings u 1))))
          chankaya-monogram (reverse
                             (sort-by val (frequencies (get-substrings c 1))))
          unicode-digram    (reverse
                             (sort-by val (frequencies (get-substrings u 2))))
          chanakya-digram   (reverse
                             (sort-by val (frequencies (get-substrings c 2))))
          unicode-trigram   (reverse
                             (sort-by val (frequencies (get-substrings u 3))))
          chanakya-trigram  (reverse
                             (sort-by val (frequencies (get-substrings c 3))))
          unicode-keys      (concat unicode-monogram )
          chanakya-keys     (concat chankaya-monogram)
          high-freq-u-keys  (remove-low-freq unicode-keys 4)
          high-freq-c-keys  (remove-low-freq chanakya-keys 4)]
      (prn high-freq-c-keys)
      (prn "===================")
      (prn high-freq-u-keys)
      (vec (map vector (map first high-freq-c-keys) (map first high-freq-u-keys)))))

#_(defn log10 [num floor-value]
    (if (= 0 num)
      floor-value
      (Math/log num)))

#_(defn get-freq [elem elements]
    (count (filter #(= elem %) elements)))

;;calculate fitness
#_(defn calculate-fitness [unicode-string deciphered-string]
    (let [unicode-trigram    (get-substrings unicode-string 4)
          deciphered-trigram (get-substrings deciphered-string 4)
          total-trigrams     (count deciphered-trigram)
          zero-value         (log10 (/ 1 (* 10 total-trigrams)) -100)]
      (reduce + (map #(log10 (get-freq % unicode-trigram) zero-value) deciphered-trigram))))
;;(calculate-fitness unicode-string (decipher-string chanakya-string initial-key))

#_(defn random-swap-value [initial]
    (let [a  (rand-int (count initial))
          b  (rand-int (count initial))
          e1 (get initial a)
          e2 (get initial b)]
      (-> initial
          (assoc a [(first e1) (last e2)])
          (assoc b [(first e2) (last e1)]))))

#_(defn random-swap-positions [initial]
    (shuffle initial))

#_(defn random-change [initial]
    (let [selector (rand-int 4)]
      (condp = selector
        0 (random-swap-value initial)
        1 (random-swap-value initial)
        2 (random-swap-value initial)
        3 (random-swap-value initial)
        ;; (random-swap-positions initial)
        )))

#_(defn iteration [max-iterations iteration-no initial-key cipher-text ideal-text]
    (if (> iteration-no max-iterations)
      initial-key
      (let [deciphered-text     (decipher-string cipher-text initial-key)
            fitness-initial-key (calculate-fitness ideal-text deciphered-text)
            updated-key         (random-change initial-key)
            new-deciphered-text (decipher-string cipher-text updated-key)
            fitness-updated-key (calculate-fitness ideal-text new-deciphered-text)]
        (prn "fitness score comparison" iteration-no fitness-initial-key fitness-updated-key)
        (if (>= fitness-updated-key fitness-initial-key)
          (iteration max-iterations (inc iteration-no) updated-key cipher-text ideal-text)
          (iteration max-iterations (inc iteration-no) initial-key cipher-text ideal-text)))))



#_(def init-key (create-initial-key unicode chanakya))
#_(def final-key (iteration 100 1 init-key chanakya-string unicode-string))
;;(prn (decipher-string chanakya-string chanakya-key))


(defn chanakya-to-unicode [text]
  (decipher-string text chanakya-key))
