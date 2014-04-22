(ns redstone.core
  (:require [aleph.tcp :as tcp :refer [tcp-client]]
            [lamina.core :refer [wait-for-result enqueue wait-for-message]]
            [gloss.core :refer [string]]
            [clojure.string :as s]
            [redstone.blocks :refer [id->block name->block]]))

(defn connect! [server]
  (let [defaults {:host "localhost"
                  :port 4711
                  :frame (string :utf-8 :delimiters ["\n"])}]
    (wait-for-result
     (tcp-client (merge defaults server)))))

(def connection
  (memoize connect!))

(defprotocol RPCArgument
  (as-rpc-arg [_]))

(extend-protocol RPCArgument
  clojure.lang.Keyword
  (as-rpc-arg [kw]
    (when-let [{:keys [id data]} (get name->block kw)]
      [id data]))

  java.lang.Number
  (as-rpc-arg [x] x)

  java.lang.String
  (as-rpc-arg [s] s)

  java.util.Map
  (as-rpc-arg [xs]
    (remove nil? ((juxt :x :y :z :id :data) xs)))

  java.util.List
  (as-rpc-arg [xs] (flatten (map as-rpc-arg xs)))

  java.lang.Boolean
  (as-rpc-arg [tf] (if tf 1 0)))

(defn send! [server command]
  (-> (connection server)
      (enqueue command)))

(defn receive! [server]
  (-> (connection server)
      (wait-for-message)))

(defn send-receive! [server command]
  (do (send! server command)
      (receive! server)))

(defn format-rpc [rpc args]
  (->> (or args [])
       as-rpc-arg
       (s/join ",")
       (format "%s(%s)" rpc)))

(defn command [rpc]
  (fn [server & args]
    (->> (format-rpc rpc args)
         (send! server))))

(defn query [rpc parse-fn]
  (fn [server & args]
    (->> (format-rpc rpc args)
         (send-receive! server)
         parse-fn)))

(defn parse-long [x]
  (Long/parseLong x))

(defn parse-double [x]
  (Double/parseDouble x))

(def listeners
  (atom {}))

(def block-hits!
  (query "events.block.hits"
         #(for [hit (remove s/blank? (s/split % #"\|"))]
            (let [parsed (->> (s/split hit #",")
                              (map parse-long)
                              (zipmap [:x :y :z :face :player-id]))]
              (-> parsed
                  (select-keys [:player-id :face])
                  (merge {:position (select-keys parsed [:z :y :x])
                          :event :block:hit}))))))

(defonce poll-events!
  (future
    (while true
      (doseq [[server handlers] @listeners
              handler handlers
              event (block-hits! server)]
        (handler server event))
      (Thread/sleep 200))))
