(ns redstone.client
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

(def player-ids
  (query "world.getPlayerIds"
         #(->> (s/split % #"\|")
               (remove s/blank?)
               (map parse-long))))

(def player-position
  (query "player.getPos"
         #(->> (s/split % #",")
               (map parse-double)
               (zipmap [:x :y :z]))))

(def player-tile-position
  (query "player.getTile"
         #(->> (s/split % #",")
               (map parse-long)
               (zipmap [:x :y :z]))))

(def get-block
  (query "world.getBlockWithData"
         #(->> (s/split % #",")
               (map parse-long)
               (zipmap [:id :data]))))

(def set-block!
  (command "world.setBlock"))

(def set-blocks!
  (command "world.setBlocks"))

(def set-player-tile-position!
  (command "player.setTile"))

(def set-camera-normal!
  (command "camera.mode.setNormal"))

(def set-camera-fixed!
  (command "camera.mode.setFixed"))

(def set-camera-follow!
  (command "camera.mode.setFollow"))

(def set-camera-position!
  (command "camera.setPos"))

(def clear-events!
  (command "events.clear"))

(def save-checkpoint!
  (command "world.checkpoint.save"))

(def restore-checkpoint!
  (command "world.checkpoint.restore"))

(def post-message!
  (command "chat.post"))

(def set-world!
  (command "world.setting"))

(def set-player!
  (command "player.setting"))
