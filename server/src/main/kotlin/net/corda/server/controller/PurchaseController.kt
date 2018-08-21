package net.corda.server.controller

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.yo.flow.PurchaseFlow
import net.corda.yo.state.PurchaseState
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletRequest

interface PurchaseController : Controller {

    fun purchases(): List<Map<String, String>> {
        val yoStateAndRefs = rpc.proxy.vaultQueryBy<PurchaseState>().states
        val yoStates = yoStateAndRefs.map { it.state.data }
        return yoStates.map { it.toJson() }
    }

    fun purchase(request: HttpServletRequest): ResponseEntity<String> {
        val targetName = request.getParameter("target")
        val propertyName = request.getParameter("property")
        val valueName = request.getParameter("value")
        requireNotNull(valueName.toIntOrNull())
        val targetX500Name = CordaX500Name.parse(targetName)
        val target = rpc.proxy.wellKnownPartyFromX500Name(targetX500Name)
            ?: throw IllegalArgumentException("Unrecognised peer.")
        val flow = rpc.proxy.startFlowDynamic(PurchaseFlow::class.java, target, propertyName, valueName.toInt())
        return try {
            flow.returnValue.getOrThrow()
            ResponseEntity.ok("You just sent a Yo! to ${target.name}")
        } catch (e: TransactionVerificationException.ContractRejection) {
            ResponseEntity.badRequest().body("The Yo! was invalid - ${e.cause?.message}")
        }
    }
}