package net.corda.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.webserver.converters.CordaX500NameConverter
import net.corda.yo.YoFlow
import net.corda.yo.YoState
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.GsonJsonParser
import org.springframework.boot.json.JacksonJsonParser
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

val SERVICE_NAMES = listOf("Controller")

@RestController
@RequestMapping("/yo") // The paths for GET and POST requests are relative to this base path.
private class RestController(@Autowired private val rpc: NodeRPCConnection) {
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
     *  Returns my peers.
     */
    @GetMapping(value = "/peers", produces = arrayOf("application/json"))
    private fun peers(): Map<String, List<String>> {
        val nodeInfo = rpc.proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                // Filter myself and the controller out of the list of peers.
                // TODO: Re-add filtering out of our node (` + myName.organisation`).
                .filter { it.organisation !in (SERVICE_NAMES) }
                .map { it.toString() })
    }

    /**
     *  Returns a list of existing Yo's.
     */
    @GetMapping(value = "/yos", produces = arrayOf("application/json"))
    // TODO: Return as JSON object
    private fun yos() = rpc.proxy.vaultQueryBy<YoState>().states.map { it.state.data.toJson() }

    /**
     *  An example POST endpoint.
     */
    @PostMapping(value = "/yo", produces = arrayOf("text/plain"), headers = arrayOf("Content-Type=application/x-www-form-urlencoded"))
    private fun yo(request: HttpServletRequest): ResponseEntity<String> {
        val targetName = request.getParameter("target")
        val targetX500Name = CordaX500Name.parse(targetName)
        val target = rpc.proxy.wellKnownPartyFromX500Name(targetX500Name) ?: throw IllegalArgumentException("Unrecognised peer.")
        val flowHandle = rpc.proxy.startFlowDynamic(YoFlow::class.java, target)
        flowHandle.returnValue.get()
        return ResponseEntity.ok("You just sent a Yo! to ${target.name}")
    }

    private fun YoState.toJson(): Map<String, String> {
        return mapOf("origin" to origin.toString(), "target" to target.toString(), "yo" to yo)
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
        val (_, updates) = rpc.proxy.vaultTrack(YoState::class.java)
        updates.subscribe { update ->
            update.produced.forEach {
                logger.info(it.toString())
                template.convertAndSend("/stompResponse", it.toString(), mapOf("content-type" to "text/plain"))
            }
        }
    }
}