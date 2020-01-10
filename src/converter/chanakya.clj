(ns converter.chanakya
  (:require [clojure.string :as s]))



(def chanakya
  ["¤","U",


   "¢ð","´ð","ð¸",

   "¥æò","¸",
   "¸·", "¸¹", "¸»", "¸Á","¸Ç", "¸É", "¸È","¸Ø","¸Ú","¸Ù",

   "A","B","C","D","E","F","G","H","I","J","K","L","M",
   "N","O","P","Q","R","T","V","W","X","Y",

   "`","a","b","d","e","f","g","h","i","j","k","l","m","n","p","q","r","s","t","u",

   "žæ","ž","#","%","@",
   "„","¦","¨","¯","µ","º",

   "Cþ","q","Ê","u","g",
   "Ÿæ","Åþ","Çþ","Éþ",
   "\\^","h","Ð","ý","þ",


   "¥ô","¥æð","¥õ","¥æñ","¥æ","¥","§Z","§ü","§","©","ª","«","¬","&shy;","°ð","°",

   "€","·","","¹","‚","»","ƒæ","ƒ","¾",
   "“","‘","¿","À","”","…","’","Á","Ûæ","Û","†æ","†",

   "Å","Æ","Ç","É","‡æ","‡",
   "ˆ","Ì","‰","Í","Î","¼","Š","Ï","óæ","ó","‹æ","Ù","‹",

   "Œ","Â","","È","Ž","Õ","","Ö","","×",
   "Ä","Ø","Ú","Ë","Ü","¶","Ý","Ã","ß",
   "àæ","³æ","o","³","à","c","á","S","â","ã","±",
   "ÿæ","ÿ","˜æ","˜","™æ","™","üð´",

   "æò","æñ","æ","è","é","ê","ä","å","ë","ì","í","Ô","ñ","ô","õ",
   "¢","´","¡","Ñ","¸","ò","ù","÷","ð","ç",
   "Z",

   "0","1","2","3","4","5","6","7","8","9",
   "®","v","w","x","y","z","\\{","\\|","\\}","~",
   "्ो","्ौ","्ाे", "्ाा","ाे","ाे","ाै","्ा","ंु","ओे","ोे","ाे","ईंं"])





(def unicode
  ["","",

   "ð¢","ð´","¸ð",

   "ऑ","फ़्",
   "क़","ख़","ग़","ज़","ड़","ढ़","फ़","य़","ऱ","ऩ",

   "्र","क्च","ष्ट","ष्ठ","श्व","स्न","त्र","॥","ढ्ढ","छ्व","्य","रु","रू",
   "हृ","ह्र","क्क","क्त","क्र","ञ्ज","ङ्क","ङ्ख","ङ्ग","ङ्घ",
   "क्व","ड्ड","ड्ढ","स्र","द्ग","द्घ","द्द","द्ध","द्ब","द्भ","द्म","द्य","द्व","ठ्ठ","श्च","ह्न","ह्म्","ह्य","ह्ल","ह्व",

   "त्त","त्त्","प्त","त्न","ञ्च",
   "ल्ल","ष्ट्व","ङ्क्ष","ख्न","द्ब्र","ख्र",

   "ष्ट्र","ह्न","ज़्","ह्व","द्द",
   "श्र","ट्र","ड्र","ढ्र",
   "ट्ट","द्ध","।","्र","्र",

   "ओ","ओ","औ","औ","आ","अ","ईं","ई","इ","उ","ऊ","ऋ","ॠ","ऌ","ऐ","ए",

   "क्", "क","ख्","ख","ग्","ग","घ","घ्","ङ",
   "च्च्","च्","च","छ","ज्ज्","ज्","ज्","ज","झ","झ्","ञ","ञ्",

   "ट","ठ","ड","ढ","ण","ण्",
   "त्","त","थ्","थ","द","द","ध्","ध","न्न","न्न्","न","न","न्",

   "प्","प","फ्","फ","ब्","ब","भ्","भ","म्","म",
   "य्","य","र","ल्","ल","ल","ळ","व्","व",
   "श","श","श","श्","श्","ष्","ष","स्","स","ह","ह्",
   "क्ष","क्ष्","त्र","त्र्","ज्ञ","ज्ञ्","ðZ",

   "ॉ","ौ","ा","ी","ु","ू","ु","ू","ृ","ॄ","ॢ","े","ै","ो","ौ",
   "ं","ं","ँ",":","़", "ॅ","ऽ","्","े", "ि",
   "ंü",

   "०","१","२","३","४","५","६","७","८","९",
   "0","1","2","3","4","5","6","7","8","9",

   "े", "ै", "े","ा","ो","ो","ौ","","ुं","ओ","ो","ो","ईं"
   ])


(def replacable-control-chars
  {0x82 "‚"
   0x83 "ƒ"
   0x84 "„"
   0x85 "…"
   0x86 "†"
   0x87 "‡"
   0x88 "ˆ"
   0x89 "‰"
   0x8a "Š"
   0x8b "‹"
   0x8c "Œ"
   0x91 "‘"
   0x92 "’"
   0x93 "“"
   0x94 "”"
   0x95 "•"
   0x96 "–"
   0x97 "—"
   0x98 "˜"
   0x99 "™"
   0x9a "š"
   0x9b "š"
   0x9c "œ"
   0x9f "Ÿ"})

(defn replace-control-chars [text mapping]
  (let [[key value] (first mapping)]
    (if (empty? (rest mapping))
      text
      (replace-control-chars
       (s/replace text (str (char key)) value)
       (rest mapping)))))

(defn replace-symbols [text mapping]
  (let [[key value] (first mapping)]
    (if (empty? (rest mapping))
      text
      (replace-symbols (s/replace text (re-pattern key) value) (rest mapping)))))


(def chanakya-unicode-mapping (for [i (range (count chanakya))]
                                [(nth chanakya i)
                                 (nth unicode i)
                                 ]))


(defn get-replacement-char [scheme to-switch-char next-char prev-char]
  (-> scheme
      (s/replace #"-" next-char)
      (s/replace #"_" prev-char)
      (s/replace "$1" (str (first to-switch-char)))
      (s/replace "$2" (str (second to-switch-char)))
      (s/replace "$3" (str (last to-switch-char)))))

(defn switch-char [full-string to-switch-char curr-index scheme]
  (let [char-index   (-> (subs full-string curr-index)
                         (s/index-of to-switch-char))
        size-of-char (count to-switch-char)]
    (if char-index
      (let [i                    (+ curr-index char-index)
            next-char            (subs full-string (+ i size-of-char) (+ i 1 size-of-char))
            prev-char            (subs full-string (- i 1) i)
            moving-forward-index (+ i size-of-char 1)]
        (switch-char
         (str (subs full-string 0 i)
              (get-replacement-char scheme to-switch-char next-char prev-char)
              (subs full-string moving-forward-index))
         to-switch-char
         moving-forward-index
         scheme))
      full-string)))


(defn add-half-r
  "half r position is moved based on prev character"
  [full-string curr-index]
  (let [matras     ["ा" "ि" "ी" "ु" "ू" "ृ" "े" "ै" "ो" "ौ" "ं" ":" "ँ" "ॅ"]
        half-r-pos (s/index-of (subs full-string curr-index) "ü")]
    (if half-r-pos
      (let [p              (+ curr-index half-r-pos)
            check-if-matra (not= -1 (.indexOf matras (subs full-string (dec p) p)))
            start-pos      (if check-if-matra (- p 2) (- p 1))
            character      (subs full-string (dec p) p)]
        (add-half-r
         (str
          (subs full-string 0 start-pos)
          "र्"
          (subs full-string start-pos p)
          (subs full-string (inc p)))
         (inc p)))
      full-string)))


(defn chanakya-to-unicode [chanakya-text]
  (->  chanakya-text
       (replace-control-chars replacable-control-chars)
       (replace-symbols chanakya-unicode-mapping)
       (switch-char "ि" 0 "-$1")
       (switch-char "ि्" 0 "$2-$1")
       (switch-char "िं्" 0 "$3-$1$2")
       (add-half-r 0)))
