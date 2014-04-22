(ns redstone.client
  (:require [clojure.string :as s]
            [redstone.core :refer [command query listeners
                                   parse-long parse-double]]))

(def players
  "Returns a sequence of connected players"
  (query "world.getPlayerIds"
         #(->> (s/split % #"\|")
               (remove s/blank?)
               (map parse-long)
               (map (partial assoc {} :id)))))

(def player-position
  "The player's position as a map of :x, :y, :z coordinates"
  (query "player.getPos"
         #(->> (s/split % #",")
               (map parse-double)
               (zipmap [:x :y :z]))))

(def player-tile-position
  "The player's tile position as a map of :x, :y, :z coordinates"
  (query "player.getTile"
         #(->> (s/split % #",")
               (map parse-long)
               (zipmap [:x :y :z]))))

(def block
  "The block at the specified position"
  (query "world.getBlockWithData"
         #(->> (s/split % #",")
               (map parse-long)
               (zipmap [:id :data]))))

(def blocks
  "The blocks between two coordinates.
   Requires RaspberryJuice >= 1.3"
  (query "world.getBlocks"
         #(->> (s/split % #",")
               (map parse-long)
               (map (partial assoc {} :id)))))

(def set-block!
  "Sets the block at the given coordinates"
  (command "world.setBlock"))

(def set-blocks!
  "Set all blocks between two coordinates"
  (command "world.setBlocks"))

(def set-player-tile-position!
  "Move the player to the given coordinates"
  (command "player.setTile"))

(def send-message!
  "Send a chat message to the server"
  (command "chat.post"))

(defn listen!
  "Specify a handler function for a given event"
  [server event handler]
  (when (= event :block:hit)
    (swap! listeners update-in [server] conj handler)
    nil))

(def clear-events!
  "Clears events from the server-side buffer.
   Call before setting up the first event listener if
   you don't want to receive events already captured"
  (command "events.clear"))

;; Not implemented in RaspberryJuice

(def set-camera-normal!
  "Not implemented in RaspberryJuice"
  (command "camera.mode.setNormal"))

(def set-camera-fixed!
  "Not implemented in RaspberryJuice"
  (command "camera.mode.setFixed"))

(def set-camera-follow!
  "Not implemented in RaspberryJuice"
  (command "camera.mode.setFollow"))

(def set-camera-position!
  "Not implemented in RaspberryJuice"
  (command "camera.setPos"))

(def save-checkpoint!
  "Not implemented in RaspberryJuice"
  (command "world.checkpoint.save"))

(def restore-checkpoint!
  "Not implemented in RaspberryJuice"
  (command "world.checkpoint.restore"))

(def set-world!
  "Not implemented in RaspberryJuice"
  (command "world.setting"))

(def set-player!
  "Not implemented in RaspberryJuice"
  (command "player.setting"))
