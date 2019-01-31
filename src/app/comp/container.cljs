
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp cursor-> action-> mutation-> <> div button textarea span]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo-md.comp.md :refer [comp-md]]
            [app.config :refer [dev?]]
            [app.generator :refer [generate-methods]]
            [cljs.reader :refer [read-string]]
            [app.generator :refer [generate-tree]]))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel), states (:states store)]
   (div
    {:style (merge ui/global ui/fullscreen ui/column)}
    (div
     {:style (merge ui/row-parted {:padding 8})}
     (span {})
     (div
      {:style ui/row-middle}
      (button
       {:style ui/button,
        :inner-text (str "Generate methods"),
        :on-click (fn [e d! m!]
          (let [rules (js->clj (js/JSON.parse (:content store)) :keywordize-keys true)]
            (d! :result (generate-methods rules))))})
      (=< 8 nil)
      (button
       {:style ui/button,
        :inner-text (str "Generate tree"),
        :on-click (fn [e d! m!]
          (let [rules (js->clj (js/JSON.parse (:content store)) :keywordize-keys true)]
            (d! :result (generate-tree rules))))})))
    (div
     {:style (merge ui/flex ui/row)}
     (textarea
      {:value (:content store),
       :placeholder "Content",
       :style (merge ui/flex ui/textarea {:font-family ui/font-code}),
       :on-input (action-> :content (:value %e))})
     (textarea
      {:value (:result store),
       :placeholder "Content",
       :style (merge ui/flex ui/textarea {:font-family ui/font-code}),
       :on-input (action-> :result (:value %e))})
     (when dev? (cursor-> :reel comp-reel states reel {}))))))
