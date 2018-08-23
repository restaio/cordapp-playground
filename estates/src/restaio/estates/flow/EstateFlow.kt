package restaio.estates.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import restaio.estates.contract.EstateContract
import restaio.estates.state.EstateState

@InitiatingFlow
@StartableByRPC
class EstateFlow(
    val name: String,
    val value: Int
) : FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = EstateFlow.tracker()

    companion object : FlowCompanion {
        object CREATING : ProgressTracker.Step("Creating a new estate!")
        object SIGNING : ProgressTracker.Step("Signing the estate!")
        object VERIFYING : ProgressTracker.Step("Verifying the estate!")
        object FINALISING : ProgressTracker.Step("Sending the estate!") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        @JvmStatic override fun tracker() = ProgressTracker(CREATING, SIGNING, VERIFYING, FINALISING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = CREATING

        val me = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val command = Command(EstateContract.Send(), listOf(me.owningKey))
        val state = EstateState(me, name, value)
        val stateAndContract = StateAndContract(state, EstateContract.ID)
        val utx = TransactionBuilder(notary).withItems(stateAndContract, command)

        progressTracker.currentStep = SIGNING
        val stx = serviceHub.signInitialTransaction(utx)

        progressTracker.currentStep = VERIFYING
        stx.verify(serviceHub)

        progressTracker.currentStep = FINALISING
        return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker()))
    }
}