package net.corda.server

import net.corda.yo.YoFlow
import net.corda.yo.YoState
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
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/yo") // The paths for GET and POST requests are relative to this base path.
private class RestController(@Autowired private val rpc: NodeRPCConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    /**
     *  An example GET endpoint.
     */
    @GetMapping(value = "/yos", produces = arrayOf("text/plain"))
    private fun yos() = ResponseEntity.ok(rpc.proxy.vaultQuery(YoState::class.java).states.toString())

    /**
     *  An example POST endpoint.
     */
    @PostMapping(value = "/yo", produces = arrayOf("text/plain"), headers = arrayOf("Content-Type=application/x-www-form-urlencoded"))
    private fun yo(request: HttpServletRequest): ResponseEntity<String> {
        val targetName = request.getParameter("target")
        val target = rpc.proxy.partiesFromName(targetName, false).iterator().next()
        val flowHandle = rpc.proxy.startFlowDynamic(YoFlow::class.java, target)
        flowHandle.returnValue.get()
        return ResponseEntity.ok("You just sent a Yo! to ${target.name}")
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
        val (snapshot, updates) = rpc.proxy.vaultTrack(YoState::class.java)
        updates.subscribe { update ->
            update.produced.forEach {
                logger.info(it.toString())
                template.convertAndSend("/stompResponse", it.toString(), mapOf("content-type" to "text/plain"))
            }
        }
    }
}