package restaio.estates.state

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

data class EstateState(
    val owner: Party,
    val name: String,
    val value: Int
) : QueryableState, State {

    override val participants: List<Party> get() = emptyList()

    override fun toString() = "${owner.name}: $name ($value)"

    override fun supportedSchemas() = listOf(EstateSchemaV1)

    override fun generateMappedObject(schema: MappedSchema) = EstateSchemaV1.PersistentEstateState(
        owner.name.toString(), name)

    override fun toJson(): Map<String, String> = mapOf(
        "owner" to owner.name.organisation,
        "name" to name,
        "value" to value.toString())

    object EstateSchema

    object EstateSchemaV1 : MappedSchema(EstateSchema.javaClass, 1, listOf(PersistentEstateState::class.java)) {
        @Entity
        @Table(name = "estates")
        class PersistentEstateState(
            @Column(name = "owner") var owner: String = "",
            @Column(name = "name") var name: String = "",
            @Column(name = "value") var value: Int = 0
        ) : PersistentState()
    }
}