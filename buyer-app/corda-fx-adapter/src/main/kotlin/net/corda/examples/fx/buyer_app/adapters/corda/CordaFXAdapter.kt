package net.corda.examples.fx.buyer_app.adapters.corda

import net.corda.client.rpc.CordaRPCClient
import net.corda.examples.fx.buyer_app.logging.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

interface FXAdapter {
    fun sayMyName(): String
}

@Component
private class CordaFXAdapter @Autowired private constructor(private val configuration: CordaNodeConfiguration) : FXAdapter {

    private val rpc = CordaRPCClient(configuration.nodeAddress)

    companion object {
        private val logger = loggerFor<CordaFXAdapter>()
    }

    override fun sayMyName(): String {
        logger.info("Connecting to CORDA node at address ${configuration.nodeAddress}")
        rpc.start(configuration.user.username, configuration.user.password).use {
            logger.info("Connected to CORDA node ${it.proxy.nodeInfo().legalIdentities[0]}!")
            logger.info("Starting flow BuyCurrency")
            return it.proxy.nodeInfo().legalIdentities.first().name.organisation
        }
    }
}