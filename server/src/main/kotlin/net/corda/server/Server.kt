package net.corda.server

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

private const val HTTP_PORT = "config.http.port"

@SpringBootApplication
private open class Server {
    /**
     * Configures the port the servlet runs on.
     */
    @Bean
    open fun setHttpPortServerRunsOn(@Value("\${$HTTP_PORT}") httpPort: Int) = EmbeddedServletContainerCustomizer {
        server -> server.setPort(httpPort)
    }

    /**
     * Registers an endpoint for STOMP messages.
     */
    @EnableWebSocketMessageBroker
    open class WebSocketConfig : AbstractWebSocketMessageBrokerConfigurer() {
        override fun registerStompEndpoints(registry: StompEndpointRegistry) {
            registry.addEndpoint("/stomp").setAllowedOrigins("*").withSockJS()
        }
    }
}

fun main(args: Array<String>) {
    val app = SpringApplication(Server::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.isWebEnvironment = true
    app.run(*args)
}