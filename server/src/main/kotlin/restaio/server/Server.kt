package restaio.server

import org.springframework.boot.Banner.Mode.OFF
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

/** Our Spring Boot application. */
@SpringBootApplication
open class Server {

    companion object {
        private lateinit var app: ConfigurableApplicationContext

        /** Starts our Spring Boot application. */
        @JvmStatic fun main(args: Array<String>) {
            app = SpringApplication(Server::class.java).apply {
                setBannerMode(OFF)
                isWebEnvironment = true
            }.run(*args)
        }

        /** For debugging purposes. */
        fun refresh() = app.refresh()
    }

    /** Registers an endpoint for STOMP messages. */
    @EnableWebSocketMessageBroker
    open class WebSocketConfig : AbstractWebSocketMessageBrokerConfigurer() {
        override fun registerStompEndpoints(registry: StompEndpointRegistry) {
            registry.addEndpoint("/stomp").withSockJS()
        }
    }
}