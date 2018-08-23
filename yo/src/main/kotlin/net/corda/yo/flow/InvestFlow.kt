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
import net.corda.yo.contract.InvestContract
import net.corda.yo.state.InvestState

@InitiatingFlow
@StartableByRPC
class InvestFlow(
    val target: Party,
    val estate: String,
    val value: Int
) : FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = InvestFlow.tracker()

    companion object {
        object CREATING : ProgressTracker.Step("Creating a new investment!")
        object SIGNING : ProgressTracker.Step("Signing the investment!")
        object VERIFYING : ProgressTracker.Step("Verifying the investment!")
        object FINALISING : ProgressTracker.Step("Sending the investment!") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(CREATING, SIGNING, VERIFYING, FINALISING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = CREATING

        val me = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val command = Command(InvestContract.Send(), listOf(me.owningKey))
        val state = InvestState(me, target, estate, value)
        val stateAndContract = StateAndContract(state, InvestContract.ID)
        val utx = TransactionBuilder(notary = notary).withItems(stateAndContract, command)

        progressTracker.currentStep = SIGNING
        val stx = serviceHub.signInitialTransaction(utx)

        progressTracker.currentStep = VERIFYING
        stx.verify(serviceHub)

        progressTracker.currentStep = FINALISING
        return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker()))
    }
}