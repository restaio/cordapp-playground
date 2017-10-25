package net.corda.server

import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.yo.YoFlow
import net.corda.yo.YoState
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

private const val CONTROLLER_NAME = "config.controller.name"

@RestController
@RequestMapping("/yo") // The paths for GET and POST requests are relative to this base path.
private class RestController(
        private val rpc: NodeRPCConnection,
        @Value("\${$CONTROLLER_NAME}") private val controllerName: String) {

    private val myName = rpc.proxy.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    /**
     *  Returns my name.
     */
    @GetMapping(value = "/me", produces = arrayOf("text/plain"))
    private fun me() = myName.toString()

    /**
     *  Returns a list of the network peers.
     */
    @GetMapping(value = "/peers", produces = arrayOf("application/json"))
    private fun peers(): Map<String, List<String>> {
        val nodeInfo = rpc.proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                // Filter myself and the controller out of the list of peers.
                // TODO: Re-add filtering out of our node (` + myName.organisation`).
                .filter { it.organisation != controllerName }
                .map { it.toString() })
    }

    /**
     *  Returns a list of existing Yo's.
     */
    @GetMapping(value = "/getYos", produces = arrayOf("application/json"))
    private fun getYos() = rpc.proxy.vaultQueryBy<YoState>().states.map { it.state.data.toJson() }

    /**
     *  Sends a Yo to a counterparty.
     */
    @PostMapping(value = "/sendYo", produces = arrayOf("text/plain"), headers = arrayOf("Content-Type=application/x-www-form-urlencoded"))
    private fun sendYo(request: HttpServletRequest): ResponseEntity<String> {
        val targetName = request.getParameter("target")
        val targetX500Name = CordaX500Name.parse(targetName)
        val target = rpc.proxy.wellKnownPartyFromX500Name(targetX500Name) ?: throw IllegalArgumentException("Unrecognised peer.")
        val flowHandle = rpc.proxy.startFlowDynamic(YoFlow::class.java, target)
        flowHandle.returnValue.get()
        return ResponseEntity.ok("You just sent a Yo! to ${target.name}")
    }

    /**
     *  Maps a YoState to a JSON object.
     */
    private fun YoState.toJson(): Map<String, String> {
        return mapOf("origin" to origin.name.organisation, "target" to target.name.toString(), "yo" to yo)
    }
}

@Controller
@MessageMapping("/stomp")
private class StompController(private val rpc: NodeRPCConnection, private val template: SimpMessagingTemplate) {
    companion object {
        private val logger = LoggerFactory.getLogger(StompController::class.java)
    }

    /**
     *  An example endpoint for responding to STOMP messages.
     */
    @MessageMapping("/streamYos")
    private fun streamYos() {
        val (_, updates) = rpc.proxy.vaultTrack(YoState::class.java)
        updates.subscribe { update ->
            update.produced.forEach {
                logger.info(it.toString())
                template.convertAndSend("/stompResponse", it.toString(), mapOf("content-type" to "text/plain"))
            }
        }
    }
}