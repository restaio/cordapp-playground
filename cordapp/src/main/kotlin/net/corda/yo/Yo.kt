package net.corda.yo

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// Flow.
@InitiatingFlow
@StartableByRPC
class YoFlow(private val target: Party) : FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = YoFlow.tracker()

    companion object {
        object CREATING : ProgressTracker.Step("Creating a new Yo!")
        object SIGNING : ProgressTracker.Step("Verifying the Yo!")
        object VERIFYING : ProgressTracker.Step("Verifying the Yo!")
        object FINALISING : ProgressTracker.Step("Sending the Yo!") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(CREATING, SIGNING, VERIFYING, FINALISING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = CREATING
        val me = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val command = Command(YoContract.Send(), listOf(me.owningKey))
        val state = YoState(me, target)
        val stateAndContract = StateAndContract(state, YO_CONTRACT_ID)
        val utx = TransactionBuilder(notary = notary).withItems(stateAndContract, command)

        progressTracker.currentStep = SIGNING
        val stx = serviceHub.signInitialTransaction(utx)

        progressTracker.currentStep = VERIFYING
        stx.verify(serviceHub)

        progressTracker.currentStep = FINALISING
        return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker()))
    }
}

// Contract and state.
const val YO_CONTRACT_ID = "net.corda.yo.YoContract"

class YoContract: Contract {

    // Command.
    class Send : TypeOnlyCommandData()

    // Contract code.
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when Yo'ing other parties." using (tx.inputs.isEmpty())
        "There must be one output: The Yo!" using (tx.outputs.size == 1)
        val yo = tx.outputsOfType<YoState>().single()
        // TODO: Re-add this constraint.
        // "No sending Yo's to yourself!" using (yo.target != yo.origin)
        "The Yo! must be signed by the sender." using (yo.origin.owningKey == command.signers.single())
    }
}

// State.
data class YoState(val origin: Party,
                   val target: Party,
                   val yo: String = "Yo!") : ContractState {
    override val participants get() = listOf(target)
    override fun toString() = "${origin.name}: $yo"
}