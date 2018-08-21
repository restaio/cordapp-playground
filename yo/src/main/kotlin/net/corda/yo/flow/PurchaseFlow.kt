package net.corda.yo.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.yo.contract.PurchaseContract
import net.corda.yo.state.PurchaseState

@InitiatingFlow
@StartableByRPC
class PurchaseFlow(
    val target: Party,
    val property: String,
    val value: Int
) : FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = PurchaseFlow.tracker()

    companion object {
        object CREATING : ProgressTracker.Step("Creating a new purchase!")
        object SIGNING : ProgressTracker.Step("Verifying the purchase!")
        object VERIFYING : ProgressTracker.Step("Verifying the purchase!")
        object FINALISING : ProgressTracker.Step("Sending the purchase!") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(CREATING, SIGNING, VERIFYING, FINALISING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = CREATING

        val me = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val command = Command(PurchaseContract.Send(), listOf(me.owningKey))
        val state = PurchaseState(me, target, property, value)
        val stateAndContract = StateAndContract(state, PurchaseContract.ID)
        val utx = TransactionBuilder(notary = notary).withItems(stateAndContract, command)

        progressTracker.currentStep = SIGNING
        val stx = serviceHub.signInitialTransaction(utx)

        progressTracker.currentStep = VERIFYING
        stx.verify(serviceHub)

        progressTracker.currentStep = FINALISING
        return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker()))
    }
}