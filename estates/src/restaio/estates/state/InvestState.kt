package restaio.estates.state

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

data class InvestState(
    val origin: Party,
    val target: Party,
    val estate: String,
    val value: Int
) : QueryableState, State {

    override val participants get() = listOf(target)

    override fun toString() = "${origin.name}: $estate ($value)"

    override fun supportedSchemas() = listOf(InvestSchemaV1)

    override fun generateMappedObject(schema: MappedSchema) =
        InvestSchemaV1.PersistentInvestState(origin.name.toString(), target.name.toString(), estate)

    override fun toJson(): Map<String, String> = mapOf(
        "origin" to origin.name.organisation,
        "target" to target.name.toString(),
        "estate" to estate,
        "value" to value.toString())

    object InvestSchema

    object InvestSchemaV1 : MappedSchema(InvestSchema.javaClass, 1, listOf(PersistentInvestState::class.java)) {
        @Entity
        @Table(name = "investment")
        class PersistentInvestState(
            @Column(name = "origin")
            var origin: String = "",
            @Column(name = "target")
            var target: String = "",
            @Column(name = "estate")
            var estate: String = "",
            @Column(name = "value")
            var value: Int = 0
        ) : PersistentState()
    }
}