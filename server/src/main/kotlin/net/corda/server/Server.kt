package net.corda.server

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
open class Server {

    companion object {

        /**
         * Starts our Spring Boot application.
         */
        @JvmStatic fun main(args: Array<String>) {
            val app = SpringApplication(Server::class.java)
            app.setBannerMode(Banner.Mode.OFF)
            app.isWebEnvironment = true
            app.run(*args)
        }
    }

    /** Registers an endpoint for STOMP messages. */
    @EnableWebSocketMessageBroker
    open class WebSocketConfig : AbstractWebSocketMessageBrokerConfigurer() {
        override fun registerStompEndpoints(registry: StompEndpointRegistry) {
            registry.addEndpoint("/stomp").withSockJS()
        }
    }
}