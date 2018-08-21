package net.corda.server

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
        @JvmStatic fun main(args: Array<String>) = launch<Server>(args) {
            setBannerMode(OFF)
            isWebEnvironment = true
        }

        /** For debugging purposes. */
        fun refresh() = app.refresh()

        private inline fun <reified T> launch(args: Array<String>, init: SpringApplication.() -> Unit) {
            app = SpringApplication(T::class.java).apply(init).run(*args)
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