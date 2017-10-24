package net.corda.examples.fx.buyer_app

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

const val HTTP_PORT = "config.http.port"

@Configuration
private open class ServletConfig @Autowired constructor(private val configuration: ServerConfiguration) {

    @Bean
    open fun containerCustomizer() = EmbeddedServletContainerCustomizer {
        container -> container.setPort(configuration.httpPort)
    }
}

interface ServerConfiguration {
    val httpPort: Int
}

@Component
private class ServerConfigurationImpl @Autowired private constructor(
        @Value("\${${HTTP_PORT}}") override val httpPort: Int) : ServerConfiguration