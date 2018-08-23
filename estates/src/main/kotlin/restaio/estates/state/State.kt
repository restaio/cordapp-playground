package restaio.estates.state

interface State {

    fun toJson(): Map<String, String>
}