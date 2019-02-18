
(ns app.generator
  (:require [cljs.reader :refer [read-string]] [clojure.string :as string])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn convert->variable [x]
  (string/replace x (re-pattern ":\\w+") (fn [y] (str "${" (subs y 1) "}"))))

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
       (map
        (fn [y]
          (let [var-name (subs y 1)]
            (str var-name (if (string/ends-with? y "Id") ":Id" ":string")))))
       (string/join ", ")))

(defn generate-field [rule base-path]
  (let [name-string (pr-str (or (:name rule) ""))
        path (:path rule)
        current-path (str base-path "/" (:path rule))
        raw-path (pr-str path)
        path-string (str "`" (convert->variable current-path) "`")
        prop-name (path->method path)
        fields-string (->> (:next rule)
                           (map (fn [rule] (generate-field rule current-path)))
                           (string/join "\n"))
        params-list (path->params current-path)
        result-obj (<<
                    "{\n  name: ~{name-string},\n  raw: ~{raw-path},\n  path: (~{params-list}) => ~{path-string},\n  go: (~{params-list}) => switchPath(~{path-string}),\n  ~{fields-string}\n}")]
    (<< "~{prop-name}: ~{result-obj},")))

(defn generate-tree [rules]
  (let [fields-string (->> rules
                           (map (fn [rule] (generate-field rule "")))
                           (string/join "\n"))]
    (<< "export let genRouter = {\n~{fields-string}\n};")))
