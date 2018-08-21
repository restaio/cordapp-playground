package net.corda.yo.contract

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import net.corda.yo.state.PurchaseState

class PurchaseContract : Contract {

    companion object {
        const val ID = "net.corda.yo.contract.PurchaseContract"
    }

    // Command.
    class Send : TypeOnlyCommandData()

    // Contract code.
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when Yo'ing other parties." using (tx.inputs.isEmpty())
        "There must be one output: The Yo!" using (tx.outputs.size == 1)
        val yo = tx.outputsOfType<PurchaseState>().single()
//        "No purchasing from yourself!" using (yo.target != yo.origin)
        "The Yo! must be signed by the sender." using (yo.origin.owningKey == command.signers.single())
    }
}