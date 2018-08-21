package net.corda.yo

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import net.corda.yo.state.YoState
import org.slf4j.Logger

class Client {

    companion object {

        val logger: Logger = loggerFor<Client>()

        @JvmStatic fun main(args: Array<String>) {
            require(args.size == 1) { "Usage: YoRPC <node address:port>" }
            val nodeAddress = NetworkHostAndPort.parse(args[0])
            val client = CordaRPCClient(nodeAddress)
            // Can be amended in the com.template.MainKt file.
            val proxy = client.start("user1", "test").proxy
            // Grab all signed transactions and all future signed transactions.
            val (transactions, futureTransactions) = proxy.internalVerifiedTransactionsFeed()
            // Log the existing Yo's and listen for new ones.
            futureTransactions.startWith(transactions).toBlocking().subscribe { transaction ->
                transaction.tx.outputs.forEach { output ->
                    val state = output.data as YoState
                    Client.logger.info(state.toString())
                }
            }
        }
    }
}