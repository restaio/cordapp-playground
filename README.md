![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# Spring web-server

This project defines a simple Spring web-server that connects to a Corda node via RPC.

The web-server retrieves an observable of the node's vault using RPC, and uses the observable to 
stream new vault states to the front-end.

# Structure:

The web server is set up to interact with the Yo! CorDapp (see https://www.corda.net/samples/), 
which is included in the project in the `yo` module.

The Spring web server is defined in the `server` module, and has two parts:

* `src/main/resources/static`, which defines the web-server's front-end
* `src/main/kotlin/net/corda/server`, which defines the web-server's back-end

The back-end has two controllers, defined in `server/src/main/kotlin/net/corda/server/Controller.kt`:

* `RestController`, which manages standard REST requests. It defines four endpoints:
    * GET `yo/me/`, to retrieve the node's identity
    * GET `yo/peers/`, to retrieve the node's network peers
    * GET `yo/getyos/`, to retrieve any Yo's from the node's vault
    * POST `yo/sendyo/`, to send a Yo to another node
    
* `StompController`, which manages the web-socket for streaming vault updates to the front-end. It 
  defines a a single endpoint, `/stomp/streamyos`. Our web front-end hits this endpoint when it 
  loads. In response, the web server starts streaming any new Yos to the frontend over a 
  web-socket
  
# Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

# Usage

## Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Running the web-servers:

Once the nodes are running, you can start the node web-servers from the command line:

* Windows: `gradlew.bat runPartyAServer` and `gradlew.bat runPartyBServer`
* Unix: `./gradlew runPartyAServer` and `./gradlew runPartyBServer`

You can also start the web-servers using the `Run PartyA Server` and `Run PartyB Server` IntelliJ 
run configurations.

In either case, we use environment variables to set:

* `server.port`, setting the web-server's HTTP port
* `config.rpc.port`, setting the RPC port the web-server will connect to the node on

## Interacting with the nodes:

Once the nodes are started, you can access the node's front-ends at the following locations:

* PartyA: `localhost:8080`
* PartyB: `localhost:8081`

Whenever you send a Yo to a node, the observable on the node's vault will stream an update to the 
web-server's back-end, causing the front-end to automatically update itself to display the new Yo.