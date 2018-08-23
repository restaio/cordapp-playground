package restaio.server.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import restaio.estates.state.InvestState
import restaio.server.NodeRPCConnection
import javax.servlet.http.HttpServletRequest

/**
 * A controller for interacting with the node via RPC.
 * Logic are implemented on interfaces with default functions.
 */
@Suppress("unused")
@RestController
@RequestMapping("/estates")
class ApiController(
    override val rpc: NodeRPCConnection,
    override val template: SimpMessagingTemplate,
    @Value("\${$NAME}") override val controllerName: String
) : InvestController {

    companion object {
        val logger = LoggerFactory.getLogger(RestController::class.java)

        private const val NAME = "config.controller.name"
        private const val TEXT_PLAIN = "text/plain"
        private const val APP_JSON = "application/json"
        private const val URL_ENCODED = "Content-Type=application/x-www-form-urlencoded"
    }

    // Upon creation, the controller starts streaming information on new Yo states to a websocket.
    // The front-end can subscribe to this websocket to be notified of updates.
    init {
        rpc.proxy.vaultTrack(InvestState::class.java).updates.subscribe { update ->
            update.produced.forEach { (state) ->
                val stateJson = state.data.toJson()
                template.convertAndSend("/stompresponse", stateJson)
            }
        }
    }

    @GetMapping(value = "/self", produces = arrayOf(TEXT_PLAIN))
    override fun self(): String = super.self()

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