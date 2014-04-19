# Redstone

A pure-Clojure interface to Minecraft: Pi Edition

![Clojure logo in Minecraft](https://raw.githubusercontent.com/henrygarner/redstone/master/doc/images/clojure-logo.png)

## Installation

Add the following dependency to your `project.clj` file:

    [redstone "0.1.0"]

## Usage

```clojure
	(require [redstone.client :as mc])

	(def server
		{:host "localhost"
		 :port 4711})

	(mc/post-message! server "Hello Minecraft")
```

## License

Copyright © 2014 Henry Garner

Distributed under the Eclipse Public License version 1.0
