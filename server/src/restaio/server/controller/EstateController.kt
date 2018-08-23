package restaio.server.controller

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.springframework.http.ResponseEntity
import restaio.estates.flow.EstateFlow
import restaio.estates.state.EstateState
import javax.servlet.http.HttpServletRequest

interface EstateController : Controller {

    fun list(): List<Map<String, String>> {
        val stateAndRefs = rpc.proxy.vaultQueryBy<EstateState>().states
        val states = stateAndRefs.map { it.state.data }
        return states.map { it.toJson() }
    }

    fun add(request: HttpServletRequest): ResponseEntity<String> {
        val name = request.getParameter("name")
        val value = request.getParameter("value")
        requireNotNull(value.toIntOrNull())
        val flow = rpc.proxy.startFlowDynamic(EstateFlow::class.java, name, value.toInt())
        return try {
            flow.returnValue.getOrThrow()
            ResponseEntity.ok("You just add $name with value $value")
        } catch (e: TransactionVerificationException.ContractRejection) {
            ResponseEntity.badRequest().body("The Yo! was invalid - ${e.cause?.message}")
        }
    }
}