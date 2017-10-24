package net.corda.server

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
// Defines the base path for the endpoints below.
@RequestMapping("/example")
private class HelloNodeController(@Autowired private val rpc: NodeRPCConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(HelloNodeController::class.java)
    }

    @RequestMapping(value = "/helloNode", method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json; charset=utf-8"))
    private fun helloNode(): ResponseEntity<String> {
        val myName = rpc.proxy.nodeInfo().legalIdentities.first().name.organisation
        return ResponseEntity.ok("Hello, it's $myName")
    }
}