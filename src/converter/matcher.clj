(ns converter.matcher
  (:require [clojure.string :as s]))

(def chanakya-key {:move               ["æ" "ð" "ि"]
                   ;;   "ै"  1
                   :consonants         ["क" "ख" "ग" "घ" "ङ" "च" "छ" "ज" "झ" "ञ" "ट" "ठ" "ड" "ढ" "ण" "ड़" "ढ़" "त" "थ" "द" "ध" "न" "प" "फ" "ब" "भ" "म" "य" "र" "ल" "व" "श" "ष" "स" "ह"  "क्ष" "त्र" "ज्ञ" "क़" "ख़" "ग़" "ज़" "ड़" "ढ़" "फ़" "य़"]
                   :matras             []
                   :symbols            [["Ï" "।"]
                                        ["Ñ" "‘"]
                                        ["Ò" "’"]
                                        ["" "—"]
                                        ["\r" "\n  "]
                                        ["\t" ""]]
                   :matra              [["åï" "ो"]
                                        ["ó" "ो"]
                                        ["ô" "ौ"]
                                        ["å" "ा"]
                                        ["æ" "ि"]
                                        ["ç" "ी"]
                                        ["è" "ु" ]
                                        ["é" "ू"]
                                        ["ï" "े"]
                                        ["Ó" "े"]
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
                                        ["अौ" "औ"]
                                        ["अों" "ओं"]
                                        ["आॅ" "ऑ"]
                                        ["्ाी" "ी"]
                                        ["एे" "ऐ"]
                                        ["U" ""]]
                   :double-letter-keys [["È·" "ढ़"]
                                        ["¦û" "ई"]
                                        ["Ç£" "फ"]
                                        ["Æ·" "ड़"]
                                        ["¶£" "क"]
                                        ["M£" "रु"]
                                        ["L£" "रू"]
                                        ["Q£" "¶£öË"]]
                   :triple-letter-keys [["¤åï" "ओ"]]
                   :single-letter-keys [
                                        ["¤" "अ"]
                                        ["¯" "ए"]
                                        ["¦" "इ"]
                                        ["Ù" "र"]
                                        ["â" "ह"]
                                        ["Ä" "ट"]
                                        ["¨" "उ"]
                                        ["Å" "ठ"]
                                        ["î" "ठ"]
                                        ["È" "ढ"]
                                        ["¿" "छ"]
                                        ["Æ" "ड"]
                                        ["m" "द्व"]
                                        ["â" "ह"]
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
                                        ["C" "ष्"]
                                        ["D" "ष्"]
                                        ["ý" "्र"]
                                        ["#" "प्त"]
                                        ["" "क्"]
                                        ["" "ख्"]
                                        ["" "ग्"]
                                        ["" "घ्"]
                                        ["" "ज्"]
                                        ["" "ण्"]
                                        ["" "त्"]
                                        ["" "थ्"]
                                        ["" "ध्"]
                                        ["" "न्"]
                                        ["" "प्"]
                                        ["" "फ्"]
                                        ["" "ब्"]
                                        ["" "भ"]
                                        ["" "म्"]
                                        ["" "च्"]
                                        ["" "ज्"]
                                        ["" "च्च"]
                                        ["\230" "ज्ञ्"]
                                        ["" "त्र्"]
                                        ["" "त्त्"]
                                        ["" "श्र्"]
                                        ["þ" "क्ष्"]
                                        ["Ê" "ल्"]
                                        ["Â" "व्"]
                                        ["ß" "श्"]
                                        ["Ú" "झ्"]
                                        ["m" "द्व"]
                                        ["" "ल्ल"]
                                        ["g" "द्द"]
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
                                    all-keys))
            new-index-v1    (min length (+ pos (count key-to-move) (count next-char)))
            ;;            _               (prn pos new-index-v1 curr-index next-char)
            after-next-char (subs full-string new-index-v1 (min length (inc new-index-v1)))
            is-halant       (= after-next-char  "्")
            new-index       (+ new-index-v1 (if is-halant (+ 1 (count after-next-char)) 0))
            ;;            _               (prn "is matra" is-matra after-next-char new-index)
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
        all-keys     (concat
                      (:consonants cipher-key)
                      (map second (get-keys-in-order cipher-key (disj (set (:order cipher-key)) :symbols :cleanup))))
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
            dec-p          (if (> p 0) (dec p) 0)
            check-if-matra (not= -1 (.indexOf matras (subs full-string (dec p) p)))
            start-pos      (cond
                             (= true check-if-matra) (dec dec-p)
                             :default                dec-p)
            character      (subs full-string dec-p p)]
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
            dec-p          (if (> p 0) (dec p) 0)
            check-if-matra (not= -1 (.indexOf matras (subs full-string dec-p p)))
            start-pos      (if check-if-matra dec-p p)
            character      (subs full-string dec-p p)]
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
       (debug debug?)
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

(def symbols [["²" "\n- "]
              ["¶" "\n  - "]
              ["\t" ""]
              ["\r" "\n  "]])

(defn add-symbols
  ([text symbols-key]
   (if (empty? symbols-key)
     text
     (let [k (first symbols-key)]
       (add-symbols
        (s/replace text (re-pattern (first k)) (last k))
        (rest symbols-key)))))
  ([text]
   (add-symbols text symbols)))
