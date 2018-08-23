package net.corda.yo.state

interface State {

    fun toJson(): Map<String, String>
}