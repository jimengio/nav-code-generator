
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp cursor-> action-> mutation-> <> a div button textarea span]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo-md.comp.md :refer [comp-md]]
            [app.config :refer [dev?]]
            [cljs.reader :refer [read-string]]
            ["copy-to-clipboard" :as copy!]
            ["@jimengio/router-code-generator" :refer [generateTree]]))

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
      (a
       {:style ui/link,
        :inner-text "Goto Prettier",
        :href "https://prettier.io/playground",
        :target "_blank",
        :on-click (fn [e d! m!]
          (.preventDefault (:event e))
          (copy! (:result store))
          (js/window.open "https://prettier.io/playground"))})
      (=< 8 nil)
      (button
       {:style ui/button,
        :inner-text (str "Generate tree"),
        :on-click (fn [e d! m!]
          (let [rules-json (js/JSON.parse (:content store))]
            (d! :result (generateTree rules-json))))})))
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
