package net.corda.yo

import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.ALICE
import net.corda.testing.ALICE_PUBKEY
import net.corda.testing.BOB
import net.corda.testing.DummyCommandData
import net.corda.testing.MINI_CORP_PUBKEY
import net.corda.testing.contracts.DUMMY_PROGRAM_ID
import net.corda.testing.contracts.DummyState
import net.corda.testing.ledger
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import net.corda.yo.contract.InvestContract
import net.corda.yo.flow.InvestFlow
import net.corda.yo.state.InvestState
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

const val TEST_ESTATE = "Test"
const val TEST_VALUE = 1234

class InvestFlowTests {
    lateinit var net: MockNetwork
    lateinit var a: StartedNode<MockNode>
    lateinit var b: StartedNode<MockNode>

    @Before
    fun setup() {
        setCordappPackages("net.corda.yo")
        net = MockNetwork()
        val nodes = net.createSomeNodes(2)
        a = nodes.partyNodes[0]
        b = nodes.partyNodes[1]
        net.runNetwork()
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
        net.stopNodes()
    }

    @Test
    fun flowWorksCorrectly() {
        val yo = InvestState(a.info.legalIdentities.first(), b.info.legalIdentities.first(), TEST_ESTATE, TEST_VALUE)
        val flow = InvestFlow(b.info.legalIdentities.first(), TEST_ESTATE, TEST_VALUE)
        val future = a.services.startFlow(flow).resultFuture
        net.runNetwork()
        val stx = future.getOrThrow()
        // Check yo transaction is stored in the storage service.
        val bTx = b.services.validatedTransactions.getTransaction(stx.id)
        assertEquals(bTx, stx)
        print("bTx == $stx\n")
        // Check yo state is stored in the vault.
        b.database.transaction {
            // Simple query.
            val bYo = b.services.vaultService.queryBy<InvestState>().states.single().state.data
            assertEquals(bYo.toString(), yo.toString())
            print("$bYo == $yo\n")
            // Using a custom criteria directly referencing schema entity attribute.
            val expression = builder { InvestState.InvestSchemaV1.PersistentInvestState::estate.equal("Test") }
            val customQuery = VaultCustomQueryCriteria(expression)
            val bYo2 = b.services.vaultService.queryBy<InvestState>(customQuery).states.single().state.data
            assertEquals(bYo2.estate, yo.estate)
            print("$bYo2 == $yo\n")
        }
    }
}

class InvestContractTests {
    @Before
    fun setup() {
        setCordappPackages("net.corda.yo", "net.corda.testing.contracts")
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
    }

    // @Test
    fun yoTransactionMustBeWellFormed() {
        // A pre-made Yo to Bob.
        val yo = InvestState(ALICE, BOB, TEST_ESTATE, TEST_VALUE)
        // Tests.
        ledger {
            // Input state present.
            transaction {
                input(DUMMY_PROGRAM_ID) { DummyState() }
                command(ALICE_PUBKEY) { InvestContract.Send() }
                output(InvestContract.ID) { yo }
                this.failsWith("There can be no inputs when Yo'ing other parties.")
            }
            // Wrong command.
            transaction {
                output(InvestContract.ID) { yo }
                command(ALICE_PUBKEY) { DummyCommandData }
                this.failsWith("")
            }
            // Command signed by wrong key.
            transaction {
                output(InvestContract.ID) { yo }
                command(MINI_CORP_PUBKEY) { InvestContract.Send() }
                this.failsWith("The Yo! must be signed by the sender.")
            }
            // Purchasing from yourself is not allowed.
            transaction {
                output(InvestContract.ID) { InvestState(ALICE, ALICE, TEST_ESTATE, TEST_VALUE) }
                command(ALICE_PUBKEY) { InvestContract.Send() }
                this.failsWith("No sending Yo's to yourself!")
            }
            transaction {
                output(InvestContract.ID) { yo }
                command(ALICE_PUBKEY) { InvestContract.Send() }
                this.verifies()
            }
        }
    }
}