package net.corda.server.controller

import net.corda.server.NodeRPCConnection
import net.corda.yo.state.InvestState
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 * A controller for interacting with the node via RPC.
 * Logic are implemented on interfaces with default functions.
 */
@RestController
@RequestMapping("/property")
class RestController(
    override val rpc: NodeRPCConnection,
    override val template: SimpMessagingTemplate,
    @Value("\${$NAME}") override val controllerName: String
) : InvestController {

    companion object {
        private const val NAME = "config.controller.name"
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    // Upon creation, the controller starts streaming information on new Yo states to a websocket.
    // The front-end can subscribe to this websocket to be notified of updates.
    init {
        rpc.proxy.vaultTrack(InvestState::class.java).updates.subscribe { update ->
            update.produced.forEach { (state) ->
                val yoStateJson = state.data.toJson()
                template.convertAndSend("/stompresponse", yoStateJson)
            }
        }
    }

    @GetMapping(
        value = "/me",
        produces = arrayOf("text/plain"))
    override fun me(): String = super.me()

    /** Returns a list of the node's network peers. */
    @GetMapping(
        value = "/peers",
        produces = arrayOf("application/json"))
    override fun peers(): Map<String, List<String>> = super.peers()

    /** Returns a list of existing investment. */
    @GetMapping(
        value = "/investment",
        produces = arrayOf("application/json"))
    override fun investment(): List<Map<String, String>> = super.investment()

    /** Invest a property from a counterparty. */
    @PostMapping(
        value = "/invest",
        produces = arrayOf("text/plain"),
        headers = arrayOf("Content-Type=application/x-www-form-urlencoded"))
    override fun invest(request: HttpServletRequest): ResponseEntity<String> = super.invest(request)
}