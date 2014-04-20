(ns redstone.client-test
  (:require [clojure.test :refer :all]
            [redstone.client :refer :all]))

(defmacro with-server-response [response & body]
  `(with-redefs [send-receive! (fn [_# _#] ~response)]
     ~@body))

(deftest test-requests
  (is (= (format-rpc "player.getBlock" [{:x 1 :y 2 :z 3}])
         "player.getBlock(1,2,3)"))
  
  (is (= (format-rpc "player.setBlock" [{:x 1 :y 2 :z 3} :cobblestone])
         "player.setBlock(1,2,3,4,0)"))
  
  (is (= (format-rpc "player.setBlock" [{:x 1 :y 2 :z 3} :red-wool])
         "player.setBlock(1,2,3,35,14)"))
  
  (is (= (format-rpc "world.setting" ["world_immutable" true])
         "world.setting(world_immutable,1)"))
  
  (is (= (format-rpc "chat.post" ["Hello, Minecraft!"])
         "chat.post(Hello, Minecraft!)")))

(deftest test-resonses
  (let [server {}]
    (with-server-response "1,2"
      (is (= (get-block server {:x 10 :y 11 :z 12}) {:id 1 :data 2})))
    
    (with-server-response "1.2,3.4,5.6"
      (is (= (player-position server) {:x 1.2 :y 3.4 :z 5.6})))

    (with-server-response "1,2,3"
      (is (= (player-tile-position server) {:x 1 :y 2 :z 3})))

    (with-server-response "1,2,3,4,10|4,3,2,1,10"
      (is (= (block-hits server) [{:event :block:hit
                                   :position {:x 1 :y 2 :z 3} :face 4 :player-id 10}
                                  {:event :block:hit
                                   :position {:x 4 :y 3 :z 2} :face 1 :player-id 10}])))))
