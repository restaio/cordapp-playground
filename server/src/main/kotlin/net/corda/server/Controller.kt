package net.corda.server

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/example") // The paths for GET and POST requests are relative to this base path.
private class RestController(@Autowired private val rpc: NodeRPCConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    /**
     *  An example GET endpoint.
     */
    @GetMapping(value = "/getEndpoint", produces= arrayOf("text/plain"))
    private fun getEndpoint(): ResponseEntity<String> {
        return ResponseEntity.ok("GET endpoint.")
    }

    /**
     *  An example POST endpoint.
     */
    @PostMapping(value = "/postEndpoint", produces= arrayOf("text/plain"))
    private fun postEndpoint(): ResponseEntity<String> {
        return ResponseEntity.ok("POST endpoint.")
    }
}

@Controller
private class StompController(@Autowired private val rpc: NodeRPCConnection, @Autowired private val template: SimpMessagingTemplate) {
    companion object {
        private val logger = LoggerFactory.getLogger(StompController::class.java)
    }

    /**
     *  An example endpoint for responding to STOMP messages.
     */
    @MessageMapping("/stompEndpoint")
    private fun stompEndpoint() {
        while (true) {
            val states = rpc.proxy.nodeInfo().legalIdentities.first().toString()
            logger.info(states)
            template.convertAndSend("/stompResponse", states, mapOf("content-type" to "text/plain"))
            Thread.sleep(1000)
        }
    }
}