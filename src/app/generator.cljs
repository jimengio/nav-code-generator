
(ns app.generator
  (:require [cljs.reader :refer [read-string]] [clojure.string :as string])
  (:require-macros [clojure.core.strint :refer [<<]]))

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
