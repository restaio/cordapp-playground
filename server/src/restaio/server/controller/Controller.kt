package restaio.server.controller

import restaio.server.NodeRPCConnection
import org.springframework.messaging.simp.SimpMessagingTemplate

interface Controller {

    val rpc: NodeRPCConnection

    val template: SimpMessagingTemplate

    val controllerName: String

    fun me(): String = myName.toString()

    fun peers(): Map<String, List<String>> {
        val nodes = rpc.proxy.networkMapSnapshot()
        val nodeNames = nodes.map { it.legalIdentities.first().name }
        val filteredNodeNames = nodeNames.filter { it.organisation !in listOf(controllerName, myName) }
        val filteredNodeNamesToStr = filteredNodeNames.map { it.toString() }
        return mapOf("peers" to filteredNodeNamesToStr)
    }

    private val myName get() = rpc.proxy.nodeInfo().legalIdentities.first().name
}