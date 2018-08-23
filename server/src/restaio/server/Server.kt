package restaio.server

import org.springframework.boot.Banner.Mode.OFF
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/** Our Spring Boot application. */
@SpringBootApplication
open class Server {

    companion object {
        private lateinit var context: ConfigurableApplicationContext

        /** Starts our Spring Boot application. */
        @JvmStatic fun main(args: Array<String>) {
            context = SpringApplication(Server::class.java).apply {
                setBannerMode(OFF)
                webApplicationType = SERVLET
            }.run(*args)
        }

        /** For debugging purposes. */
        fun refresh() = context.refresh()
    }

    /** Registers an endpoint for STOMP messages. */
    @Suppress("unused")
    @EnableWebSocketMessageBroker
    open class WebSocketConfig : WebSocketMessageBrokerConfigurer {
        override fun registerStompEndpoints(registry: StompEndpointRegistry) {
            registry.addEndpoint("/stomp").withSockJS()
        }
    }
}