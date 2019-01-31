
(ns app.generator
  (:require [cljs.reader :refer [read-string]] [clojure.string :as string])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn convert->variable [x]
  (string/replace x (re-pattern ":\\w+") (fn [y] (str "${" (subs y 1) "}"))))

(defn has-params? [x] (re-find (re-pattern ":\\w+") x))

(defn path->method [x]
  (let [result (-> x
                   (string/replace (re-pattern "/?:\\w+") (fn [y] "_"))
                   (string/replace "/" "-")
                   (string/replace
                    (re-pattern "-\\w")
                    (fn [y] (string/upper-case (subs y 1)))))]
    (if (string/blank? result) "_" result)))

(defn path->params [x]
  (->> (re-seq (re-pattern ":\\w+") x)
       (map (fn [y] (let [var-name (subs y 1)] (str var-name ":Id"))))
       (string/join ", ")))

(defn generate-field [rule base-path]
  (let [name-string (pr-str (or (:name rule) ""))
        path (:path rule)
        current-path (str base-path "/" (:path rule))
        raw-path (pr-str current-path)
        path-string (str "`" (convert->variable current-path) "`")
        prop-name (path->method path)
        fields-string (->> (:next rule)
                           (map (fn [rule] (generate-field rule current-path)))
                           (string/join "\n"))
        result-obj (<<
                    "{\n  name: ~{name-string},\n  path: ~{path-string},\n  ~{fields-string}\n}")]
    (if (has-params? path)
      (let [params-string (path->params path)]
        (<< "~{prop-name}: (~{params-string}) => {\n  return ~{result-obj};\n},"))
      (<< "~{prop-name}: ~{result-obj},"))))

(defn generate-method [method pathname]
  (let [params (->> (re-seq (re-pattern ":\\w+") pathname))
        params-list (->> params (map (fn [x] (str (subs x 1) ": Id"))) (string/join ", "))
        f-name (as->
                pathname
                p
                (string/replace p (re-pattern "/:\\w+") "$")
                (string/replace
                 p
                 (re-pattern "-\\w")
                 (fn [x] (str (string/upper-case (subs x 1 2)) (subs x 2))))
                (string/replace p "/" "_")
                (string/replace p "_plants$" "$"))
        pathname-text (string/replace
                       pathname
                       (re-pattern ":\\w+")
                       (fn [x] (<< "${~(subs x 1)}")))]
    (<<
     "export function go~{f-name}(~{params-list}) {\n  switchPath(`~{pathname-text}`);\n}\n\nexport function path~{f-name}(~{params-list}) {\n  return `~{pathname-text}`;\n}\n")))

(defn traverse-rules! [prefix base-path rules collect!]
  (doseq [rule rules]
    (let [this-name (or (:name rule) (:path rule))
          current-name (str prefix "---" this-name)
          current-path (str base-path "/" (:path rule))]
      (collect! {:method this-name, :path current-path})
      (traverse-rules! current-name current-path (:next rule) collect!))))

(defn generate-methods [rules]
  (let [*router-entries (atom []), collect-entry! (fn [x] (swap! *router-entries conj x))]
    (traverse-rules! "" "" rules collect-entry!)
    (let [methods (->> @*router-entries
                       (map (fn [entry] (generate-method (:method entry) (:path entry))))
                       (string/join "\n"))]
      methods)))

(defn generate-tree [rules]
  (let [fields-string (->> rules
                           (map (fn [rule] (generate-field rule "")))
                           (string/join "\n"))]
    (<< "export let genRouter = {\n~{fields-string}\n};")))

(defn path-params->method [] )
