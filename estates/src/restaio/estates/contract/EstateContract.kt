package restaio.estates.contract

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import restaio.estates.state.EstateState

class EstateContract : Contract {

    companion object {
        const val ID = "restaio.estates.contract.EstateContract"
    }

    // Command.
    class Send : TypeOnlyCommandData()

    // Contract code.
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when Yo'ing other parties." using (tx.inputs.isEmpty())
        "There must be one output: The Yo!" using (tx.outputs.size == 1)
        val yo = tx.outputsOfType<EstateState>().single()
//        "No purchasing from yourself!" using (yo.target != yo.owner)
        "The Yo! must be signed by the sender." using (yo.owner.owningKey == command.signers.single())
    }
}