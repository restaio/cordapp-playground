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
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.config.annotation.EnableWebMvc



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
     * Registers an endpoint for STOMP messages, disabling CORS.
     */
    @EnableWebSocketMessageBroker
    open class WebSocketConfig : AbstractWebSocketMessageBrokerConfigurer() {
        override fun registerStompEndpoints(registry: StompEndpointRegistry) {
            registry.addEndpoint("/stomp").setAllowedOrigins("*").withSockJS()
        }
    }

    /**
     * Disables CORS for REST.
     */
    @EnableWebMvc
    open class WebConfig : WebMvcConfigurerAdapter() {
        override fun addCorsMappings(registry: CorsRegistry?) {
            registry!!.addMapping("/**")
        }
    }
}

fun main(args: Array<String>) {
    val app = SpringApplication(Server::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.isWebEnvironment = true
    app.run(*args)
}