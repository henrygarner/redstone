(ns redstone.blocks
  (:require [clojure.java.io :refer [resource]]
            [clojure.tools.reader.edn :as edn]))

(def synonyms
  {:white-wool      :wool
   :orange-wool     :wool:1
   :magenta-wool    :wool:2
   :light-blue-wool :wool:3
   :yellow-wool     :wool:4
   :lime-wool       :wool:5
   :pink-wool       :wool:6
   :gray-wool       :wool:7
   :light-gray-wool :wool:8
   :cyan-wool       :wool:9
   :purple-wool     :wool:10
   :blue-wool       :wool:11
   :brown-wool      :wool:12
   :green-wool      :wool:13
   :red-wool        :wool:14
   :black-wool      :wool:15})

(def block-data
  (edn/read-string (slurp (resource "blocks.edn"))))

(def name->block
  (let [blocks-map (into {} (map (juxt :name identity) block-data))
        block-synonyms (into {} (map (fn [[k v]] [k (get blocks-map v)]) synonyms))]
    (merge block-synonyms blocks-map))) 

(def id->block
  (->> block-data
       (filter #(-> % :data (= 0)))
       (map (juxt :id identity))
       (into {})))
