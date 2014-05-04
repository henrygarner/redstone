# Redstone

A Clojure interface to [Minecraft: Pi Edition](http://pi.minecraft.net/).

![Clojure logo in Minecraft](https://raw.githubusercontent.com/henrygarner/redstone/master/doc/images/clojure-logo.png)

## Installation

Add the following dependency to your `project.clj` file:

```clojure
    [redstone "0.1.4"]
```

## Usage

```clojure
    (require [redstone.client :as mc])

    (def server
      {:host "localhost"
       :port 4711})

    (mc/send-message! server "Hello Minecraft")
```

## Examples

[Minecraft: Pi Edition](http://pi.minecraft.net/) supports a handful of commands for querying and updating the player's position and blocks in the world.

Positions are maps of x, y, z coordinates.

```clojure
    user=> (mc/player-position server)
	{:x 120.767 :y 44.252 :z 25.235}

    user=> (mc/player-tile-position server)
	{:x 121 :y 44 :z 25}
```

Blocks types are represented by maps of id and data.

```clojure
    ;; Get the block the player is standing on
    user=> (let [player-position       (mc/player-tile-position server)
                 position-under-player (update-in player-position [:y] dec)]
             (-> server
			     (mc/block-at position-under-player))
    {:data 0 :id 2}
```

If you already know the block id of the block you want to place you can use its id and data values directly. Alternatively you can provide the block's name as a symbol. A complete list of available block types is [here](http://minecraft.gamepedia.com/Data_values/Block_IDs).

```clojure
    user=> (def position {:x 25 :y 55 :z 22})
	#'user/position

    user=> (mc/set-block-at! server position {:id 4 :data 0})
	nil

    ;; ...is equivalent to
	user=> (mc/set-block-at! server position :cobblestone)
	nil

	;; Block names are kebab-case...
	user=> (mc/set-block-at! server position :red-flower)
	nil

	;; ...with optional data values (:red-flower:4 is a Tulip)
	user=> (mc/set-block-at! server position :red-flower:4)
	nil
```

In addition to [standard block names](http://minecraft.gamepedia.com/Data_values/Block_IDs), convenience names are also provided for coloured wool blocks.

```clojure
    user=> (mc/set-block-at! server position :orange-wool)
	nil

    ;; ...where the block type is one of
	;; {:white|:orange|:magenta|:light-blue|:yellow|:lime|:pink|:gray|:light-gray|:cyan|:purple|:blue|:brown|:green|:red|:black}-wool
```

### Events

You can set up event listeners by calling `listen!` with an event and handler function. The handler function will be called with the server and event data each time the event occurs.

```clojure
    user=> (defn event-handler [server event]
             (prn event))
    #'user/event-handler

    user=> (mc/listen! server :block:hit event-handler)
    nil

    ;; Hit a block with a sword
	{:event :block:hit, :position {:x 1, :y 2, :z 3}, :face 4, :player-id 10}
	
	user=> (defn midas-touch [server event]
	         (mc/set-block-at! server (:position event) :gold-block))
	#'user/midas-touch

    ;; Turn everything to gold!
	user=> (mc/listen! server :block:hit midas-touch)
	nil
```

`:block:hit` is the only event currently emitted.

## More Examples

For inspiration check out the [Minecraft Sketchpad](https://github.com/henrygarner/minecraft-sketchpad) repository.


## No Raspberry Pi?

Even you don't have a Raspberry Pi you can still run a version of the game which supports the Pi Edition API.

This involves:

* Running a [Craftbukkit server](http://dl.bukkit.org/)
* Installing the [RaspberryJuice](http://dev.bukkit.org/bukkit-plugins/raspberryjuice/) plugin to emulate Pi support

Comprehensive instructions for setting up a Craftbukkit server with RaspberryJuice are [available here](http://blog.lostbearlabs.com/2013/04/25/using-the-minecraft-api-without-a-raspberry-pi-craftbukkit-and-raspberryjuice/).

## License

Copyright © 2014 Henry Garner

Distributed under the Eclipse Public License version 1.0
