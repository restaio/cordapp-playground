package net.corda.yo.state

import net.corda.core.contracts.ContractState
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
    val property: String,
    val value: Int
) : ContractState, QueryableState {
    override val participants get() = listOf(target)
    override fun toString() = "${origin.name}: $property ($value)"
    override fun supportedSchemas() = listOf(InvestSchemaV1)
    override fun generateMappedObject(schema: MappedSchema) =
        InvestSchemaV1.PersistentInvestState(origin.name.toString(), target.name.toString(), property)

    object InvestSchema

    object InvestSchemaV1 : MappedSchema(InvestSchema.javaClass, 1, listOf(PersistentInvestState::class.java)) {
        @Entity
        @Table(name = "investment")
        class PersistentInvestState(
            @Column(name = "origin")
            var origin: String = "",
            @Column(name = "target")
            var target: String = "",
            @Column(name = "property")
            var property: String = "",
            @Column(name = "value")
            var value: Int = 0
        ) : PersistentState()
    }
}