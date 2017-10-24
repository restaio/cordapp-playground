package net.corda.server

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.context.annotation.Bean

private const val HTTP_PORT = "config.http.port"

@SpringBootApplication
private open class Server {
    @Bean
    open fun setHttpPortServerRunsOn(@Value("\${$HTTP_PORT}") httpPort: Int) = EmbeddedServletContainerCustomizer {
        server -> server.setPort(httpPort)
    }
}

fun main(args: Array<String>) {
    val app = SpringApplication(Server::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.isWebEnvironment = true
    app.run(*args)
}