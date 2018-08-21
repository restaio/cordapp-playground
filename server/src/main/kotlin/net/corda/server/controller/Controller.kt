package net.corda.server.controller

import net.corda.server.NodeRPCConnection
import net.corda.yo.state.PurchaseState
import org.springframework.messaging.simp.SimpMessagingTemplate

interface Controller {

    val rpc: NodeRPCConnection

    val template: SimpMessagingTemplate

    val controllerName: String

    private val myName get() = rpc.proxy.nodeInfo().legalIdentities.first().name

    fun me(): String = myName.toString()

    fun peers(): Map<String, List<String>> {
        val nodes = rpc.proxy.networkMapSnapshot()
        val nodeNames = nodes.map { it.legalIdentities.first().name }
        val filteredNodeNames = nodeNames.filter { it.organisation !in listOf(controllerName, myName) }
        val filteredNodeNamesToStr = filteredNodeNames.map { it.toString() }
        return mapOf("peers" to filteredNodeNamesToStr)
    }

    /** Maps a PurchaseState to a JSON object. */
    fun PurchaseState.toJson(): Map<String, String> = mapOf(
        "origin" to origin.name.organisation,
        "target" to target.name.toString(),
        "property" to property,
        "value" to value.toString())
}