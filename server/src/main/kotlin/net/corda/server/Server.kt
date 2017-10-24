package net.corda.server

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

private const val HTTP_PORT = "config.http.port"

@SpringBootApplication
private open class Server {
    /**
     * Configures the port the servlet runs on.
     */
    @Configuration
    open class ServletConfig(@Value("\${$HTTP_PORT}") val httpPort: Int) : EmbeddedServletContainerCustomizer {
        override fun customize(container: ConfigurableEmbeddedServletContainer?) {
            container?.setPort(httpPort)
        }
    }

    /**
     * Disables CORS protection.
     */
    @Configuration
    open class WebConfig : WebMvcConfigurerAdapter() {
        override fun addCorsMappings(registry: CorsRegistry?) {
            registry!!.addMapping("/**")
        }
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
}

fun main(args: Array<String>) {
    val app = SpringApplication(Server::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.isWebEnvironment = true
    app.run(*args)
}