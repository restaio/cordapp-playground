package net.corda.server

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

private const val CORDA_USER_NAME = "config.rpc.username"
private const val CORDA_USER_PASSWORD = "config.rpc.password"
private const val CORDA_NODE_HOST = "config.rpc.host"
private const val CORDA_NODE_RPC_PORT = "config.rpc.port"

@RestController
// Defines the base path for the endpoints below.
@RequestMapping("/example")
// TODO: Simplify the syntax for these
private class CustomerController(
        @Value("\${$CORDA_NODE_HOST}") host: String,
        @Value("\${$CORDA_NODE_RPC_PORT}") rpcPort: Int,
        @Value("\${$CORDA_USER_NAME}") val username: String,
        @Value("\${$CORDA_USER_PASSWORD}") val password: String) {

    // TODO: Abstract this away
    private val rpcAddress = NetworkHostAndPort(host, rpcPort)
    private val rpc = CordaRPCClient(rpcAddress)
    private val connection = rpc.start(username, password)

    companion object {
        private val logger = LoggerFactory.getLogger(CustomerController::class.java)
    }

    @RequestMapping(value = "/helloNode", method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json; charset=utf-8"))
    private fun helloNode(): ResponseEntity<String> {
        val myName = connection.proxy.nodeInfo().legalIdentities.first().name.organisation
        return ResponseEntity.ok("Hello, it's $myName")
    }
}