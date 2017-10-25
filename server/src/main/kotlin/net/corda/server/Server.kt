package net.corda.server

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

private const val HTTP_PORT = "config.http.port"

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
private open class Starter {
    /** Configures the port the servlet runs on. */
    @Configuration
    open class ServletConfig(@Value("\${$HTTP_PORT}") val httpPort: Int) : EmbeddedServletContainerCustomizer {
        override fun customize(container: ConfigurableEmbeddedServletContainer?) {
            container?.setPort(httpPort)
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

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Starter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.isWebEnvironment = true
    app.run(*args)
}