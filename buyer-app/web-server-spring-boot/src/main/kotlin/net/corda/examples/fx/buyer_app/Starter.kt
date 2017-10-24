package net.corda.examples.fx.buyer_app

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

/** This annotation serves as a combination of:
 * - @Configuration, designating the class as a configuration class
 * - @ComponentScan, enabling component-scanning so that the web controller classes and other
 *   components will be automatically discovered
 * - @EnableAutoConfiguration, enabling auto-configuration
*/
@SpringBootApplication
private open class Starter

// Marking this as private would prevent us from running it.
internal fun main(args: Array<String>) {

    // Creating the application.
    val application = SpringApplication(Starter::class.java)

    // Customising the application.
    application.setBannerMode(Banner.Mode.OFF)
    application.isWebEnvironment = true

    // Running the application.
    application.run(*args)
}