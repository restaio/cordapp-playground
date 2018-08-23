package net.corda.server.controller

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.yo.flow.InvestFlow
import net.corda.yo.state.InvestState
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletRequest

interface InvestController : Controller {

    fun investment(): List<Map<String, String>> {
        val stateAndRefs = rpc.proxy.vaultQueryBy<InvestState>().states
        val states = stateAndRefs.map { it.state.data }
        return states.map { it.toJson() }
    }

    fun invest(request: HttpServletRequest): ResponseEntity<String> {
        val targetName = request.getParameter("target")
        val estateName = request.getParameter("estate")
        val valueName = request.getParameter("value")
        requireNotNull(valueName.toIntOrNull())
        val targetX500Name = CordaX500Name.parse(targetName)
        val target = rpc.proxy.wellKnownPartyFromX500Name(targetX500Name)
            ?: throw IllegalArgumentException("Unrecognised peer.")
        val flow = rpc.proxy.startFlowDynamic(InvestFlow::class.java, target, estateName, valueName.toInt())
        return try {
            flow.returnValue.getOrThrow()
            ResponseEntity.ok("You just invest $estateName from ${target.name} for $valueName")
        } catch (e: TransactionVerificationException.ContractRejection) {
            ResponseEntity.badRequest().body("The Yo! was invalid - ${e.cause?.message}")
        }
    }
}