package net.corda.yo.state

import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

data class PurchaseState(
    val origin: Party,
    val target: Party,
    val property: String = "Test",
    val value: Int = 1234
) : ContractState, QueryableState {
    override val participants get() = listOf(target)
    override fun toString() = "${origin.name}: $property ($value)"
    override fun supportedSchemas() = listOf(PurchaseSchemaV1)
    override fun generateMappedObject(schema: MappedSchema) =
        PurchaseSchemaV1.PersistentPurchaseState(origin.name.toString(), target.name.toString(), property)

    object PurchaseSchema

    object PurchaseSchemaV1 : MappedSchema(PurchaseSchema.javaClass, 1, listOf(PersistentPurchaseState::class.java)) {
        @Entity
        @Table(name = "purchases")
        class PersistentPurchaseState(
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