# Welcome to machtest

This is a scratchpad project to experiment with approaches for database access on [Macchiato](http://yogthos.net/posts/2016-11-30-Macchiato.html).   Treat this project as back-of-the-napkin scribblings. The whole point is to try different things and see what ends up being cleaner and more Clojuric.

Chances are little or none of the code you'll see has been library-fied.

## This code's a mess!

Think things could be done better? Great, reach out! You can find us on:

- [#macchiato on Matrix](https://riot.im/app/#/room/#macchiato:matrix.org), which mirrors
- [#macchiato on Clojurians](https://clojurians.slack.com/archives/macchiato)

While the Clojurians channel is still the canonical endpoint, it is subject to Slack's periodic history deletion. You can find the full channel history [on Matrix/Riot](https://riot.im/app/#/room/#macchiato:matrix.org).

## What's still missing

Almost everything.

- Picking an approach to futures/promises (although the [fiber](https://github.com/laverdet/node-fibers)-based one looks promising)
- Extracting database access into its own namespace so we can easily turn it into a library
- Migrations
- Automatic type conversion. Current queries return UUIDs as strings.
- A [HugSQL](https://hugsql.org) equivalent

## Prequisites

- [Node.js](https://nodejs.org/en/) needs to be installed to run the application.
- You'll need a PostgreSQL database running on the standard port. Check namespace `machtest.db` for the default database configuration.
- Run the `setup-dev-db.sh` script from the project's root folder to create a user, a sample database and table, and some data.

## Building and running

### running in development mode

run the following command in the terminal to install NPM modules and start Figwheel:

```
lein build
```

run `node` in another terminal:

```
node target/out/machtest.js
```

Check `machtest.routes` for the http entry points.

#### configuring the REPL

Once Figwheel and node are running, you can connect to the remote REPL at `localhost:7000`.
Type `(cljs)` in the REPL to connect to Figwheel ClojureSrcipt REPL.


### building the release version

Whereas the dev version has only the one thread, the release version uses a cluster. To build:

```
lein package
```

Run the release version:

```
node target/release/machtest.js
```

# License 

Distributed under the [MIT License](https://tldrlegal.com/license/mit-license)