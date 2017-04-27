(ns clj-umaclip.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.logger :as logger]
            [ring.middleware.reload :as reload]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:use org.httpkit.server)
  (:gen-class))

(defonce clients (atom #{}))

(defn get-channels
  []
  (map :channel @clients))

(defn get-clients-with-key
  [key]
  (filter #(= (:key %) key) @clients))

(defn get-channels-with-key
  [key]
  (map :channel (get-clients-with-key key)))

(defn add-client!
  [key channel]
  (swap! clients conj {:key key :channel channel}))

(defn remove-client!
  [key channel]
  (swap!
    clients
    {:key key :channel channel}))

(defn broadcast
  [key message]
  (do
    (doseq [channel (get-channels-with-key key)]
      (send! channel message))))

(defn on-channel-close!
  [key channel]
  (remove-client! key channel))

(defn on-channel-open!
  [channel key]
  (add-client! key channel))

(defn on-channel-receive
  [key data]
  (broadcast key
    (json/write-str
      (json/read-str data :key-fn keyword))))

(defn ws-handler
  [req]
  (let [{{key :key } :params} req]
    (with-channel req channel
      (on-close channel (partial on-channel-close! key))
      (if (websocket? channel)
        (on-channel-open! channel key))
      (on-receive channel (partial on-channel-receive key)))))

(defroutes app-routes
  (route/resources "/")
  (GET "/ws/:key" [key] ws-handler)
  (route/not-found "Not Found"))

(defn handle-state-change
  [state]
  (broadcast (json/write-str state)))

(defn read-config
  [config-file]
  (json/read-str
    (slurp config-file)
    :key-fn keyword))

(def config
  { :port 8080 })

(defn -main
  [& args]
  (org.apache.log4j.BasicConfigurator/configure)
  (println "Starting server at port " (:port config))
    (run-server
      (logger/wrap-with-logger (reload/wrap-reload #'app-routes))
        {:port (:port config)}))

(def app
  (wrap-defaults app-routes site-defaults))
