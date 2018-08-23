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
@RequestMapping("/estates")
class RestController(
    override val rpc: NodeRPCConnection,
    override val template: SimpMessagingTemplate,
    @Value("\${$NAME}") override val controllerName: String
) : InvestController {

    private companion object {
        const val NAME = "config.controller.name"
        val logger = LoggerFactory.getLogger(RestController::class.java)

        const val TEXT_PLAIN = "text/plain"
        const val APP_JSON = "application/json"
        const val URL_ENCODED = "Content-Type=application/x-www-form-urlencoded"
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

    @GetMapping(value = "/me", produces = arrayOf(TEXT_PLAIN))
    override fun me(): String = super.me()

    /** Returns a list of the node's network peers. */
    @GetMapping(value = "/peers", produces = arrayOf(APP_JSON))
    override fun peers(): Map<String, List<String>> = super.peers()

    /** Returns a list of existing investment. */
    @GetMapping(value = "/investment", produces = arrayOf(APP_JSON))
    override fun investment(): List<Map<String, String>> = super.investment()

    /** Invest a estate from a counterparty. */
    @PostMapping(value = "/invest", produces = arrayOf(TEXT_PLAIN), headers = arrayOf(URL_ENCODED))
    override fun invest(request: HttpServletRequest): ResponseEntity<String> = super.invest(request)
}