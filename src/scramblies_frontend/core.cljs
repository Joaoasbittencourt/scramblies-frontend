(ns scramblies-frontend.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [ajax.core :refer [GET]]))

(def url-scramble "http://localhost:4000/api/scramble")

(defonce state
  (r/atom {:result nil
           :loading false
           :error nil
           :input "ktsea"
           :target "steak"}))

(defn update-state! [new-state]
  (swap! state merge new-state))

(defn get-scrambled-result! []
  (when (not (:loading @state))
    (update-state! {:result nil :loading true :error nil})
    (GET url-scramble
      {:keywords? true
       :response-format :json
       :params (select-keys @state [:input :target])
       :handler #(update-state! {:result {:valid? (boolean %)} :loading false})
       :error-handler #(update-state! {:result nil :loading false :error %})})))

(defn main-page []
  [:div.container.mx-auto.p-10
   [:h1 "Welcome to Scramblies!"]
   [:div.container
    [:div.card.mb
     [:div.vertical
      [:input.mb
       {:placeholder "Scrambled Word"
        :type "text"
        :value (:input @state)
        :on-change #(update-state! {:input (-> % .-target .-value)
                                    :result nil})}]
      [:input.mb
       {:placeholder "Target Word"
        :type "text"
        :disabled (:loading @state)
        :value (:target @state)
        :on-change #(update-state! {:target (-> % .-target .-value)
                                    :result nil})}]]
     [:button
      {:on-click get-scrambled-result!
       :disabled (or (:loading @state)
                     (empty? (:input @state))
                     (empty? (:target @state)))}
      "Test!"]]

    (when (:error @state)
      [:div.card.error
       [:span.card-title "Error"]
       [:p
        "Something happend... Try again later."]])

    (when (:result @state)
      (if (:valid? (:result @state))
        [:div.card.success
         [:span.card-title "Success!"]
         [:p
          [:strong (:input @state)]
          " can be rearranged to match "
          [:strong (:target @state)]]]
        [:div.card.not-valid
         [:span.card-title "Invalid..."]
         [:p
          [:strong (:input @state)]
          " cannot be rearranged to match "
          [:strong (:target @state)]]]))]])

(defn ^:export init! []
  (d/render [main-page]
            (.getElementById js/document "app")))
