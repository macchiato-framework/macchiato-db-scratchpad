# Welcome to the Macchiato DB Scratchpad

This is a scratchpad project to experiment with approaches for database access on [Macchiato](http://yogthos.net/posts/2016-11-30-Macchiato.html).  Treat this project as back-of-the-napkin scribblings. The whole point is to try different things and see what ends up being cleaner and more Clojuric.

Chances are little or none of the code you'll see here has been library-fied. As sections reach that point, we'll move them into independent projects in the [Macchiato Framework](https://github.com/macchiato-framework/) organization.

## This code's a mess!

Think things could be done better? Great, reach out! You can find us on:

- [#macchiato on Matrix](https://riot.im/app/#/room/#macchiato:matrix.org), which mirrors
- [#macchiato on Clojurians](https://clojurians.slack.com/archives/macchiato)

While the Clojurians channel is still the canonical endpoint, it is subject to Slack's periodic history deletion. You can find the full channel history [on Matrix/Riot](https://riot.im/app/#/room/#macchiato:matrix.org).

## What's still missing

Almost everything.

- Extracting database access into its own namespace so we can easily turn it into a library
- Tests
- Migrations ([postgrator](https://github.com/rickbergfalk/postgrator) might be useful here)

TODO: 

- A more polished [HugSQL](https://hugsql.org) equivalent. A draft version which gets queries from a set of files is at [`macchiato.sql`](https://github.com/macchiato-framework/macchiato-sql).
- Further review of type coercion. There's a working example of type parsers on [`machtest.db`](https://github.com/macchiato-framework/macchiato-db-scratchpad/blob/master/src/machtest/db.cljs)

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

Run `node` in another terminal:

```
node target/out/machtest.js
```

Check `machtest.routes` for the http entry points and `machtest.db` for the database functions.

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
