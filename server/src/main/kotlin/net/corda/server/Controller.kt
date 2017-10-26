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

/**
 *  A controller for REST calls.
 */
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
     *  Returns the node's name.
     */
    @GetMapping(value = "/me", produces = arrayOf("text/plain"))
    private fun me() = myName.toString()

    /**
     *  Returns a list of the node's network peers.
     */
    @GetMapping(value = "/peers", produces = arrayOf("application/json"))
    private fun peers(): Map<String, List<String>> {
        val nodes = rpc.proxy.networkMapSnapshot()
        val nodeNames = nodes.map { it.legalIdentities.first().name }
        val filteredNodeNames = nodeNames.filter { it.organisation !in listOf(controllerName, myName) }
        val filteredNodeNamesToStr = filteredNodeNames.map { it.toString() }
        return mapOf("peers" to filteredNodeNamesToStr)
    }

    /**
     *  Returns a list of existing Yo's.
     */
    @GetMapping(value = "/getyos", produces = arrayOf("application/json"))
    private fun getYos(): List<Map<String, String>> {
        val yoStateAndRefs = rpc.proxy.vaultQueryBy<YoState>().states
        val yoStates = yoStateAndRefs.map { it.state.data }
        return yoStates.map { it.toJson() }
    }

    /**
     *  Sends a Yo to a counterparty.
     */
    @PostMapping(value = "/sendyo", produces = arrayOf("text/plain"), headers = arrayOf("Content-Type=application/x-www-form-urlencoded"))
    private fun sendYo(request: HttpServletRequest): ResponseEntity<String> {
        val targetName = request.getParameter("target")
        val targetX500Name = CordaX500Name.parse(targetName)
        val target = rpc.proxy.wellKnownPartyFromX500Name(targetX500Name) ?: throw IllegalArgumentException("Unrecognised peer.")
        rpc.proxy.startFlowDynamic(YoFlow::class.java, target).returnValue.get()
        return ResponseEntity.ok("You just sent a Yo! to ${target.name}")
    }

    /**
     *  Maps a YoState to a JSON object.
     */
    private fun YoState.toJson(): Map<String, String> {
        return mapOf("origin" to origin.name.organisation, "target" to target.name.toString(), "yo" to yo)
    }
}

/**
 *  A controller for websocket messages.
 */
@Controller
@MessageMapping("/stomp") // The paths for STOMP messages are relative to this base path.
private class StompController(private val rpc: NodeRPCConnection, private val template: SimpMessagingTemplate) {
    companion object {
        private val logger = LoggerFactory.getLogger(StompController::class.java)
    }

    /**
     *  Starts streaming notifications for new Yo's over a websocket.
     */
    @MessageMapping("/streamyos")
    private fun streamYos() {
        val (_, observable) = rpc.proxy.vaultTrack(YoState::class.java)
        observable.subscribe { update ->
            update.produced.forEach {
                // Hitting the stompResponse endpoint simply causes the node to reload its list of
                // Yo's from the GET endpoint. We therefore leave the payload empty.
                template.convertAndSend("/stompresponse", "")
            }
        }
    }
}