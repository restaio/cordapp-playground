package net.corda.examples.fx.buyer_app

import net.corda.core.utilities.loggerFor
import net.corda.examples.fx.buyer_app.adapters.corda.FXAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

// Means the controller will be picked up by component-scanning.
@RestController
// Defines the base path for the individual paths below.
@RequestMapping("/api")
// @Autowired means the constructor will have a dependency injected automatically.
private class CustomerController @Autowired constructor(private val adapter: FXAdapter) {

    companion object {
        private val logger = loggerFor<CustomerController>()
    }

    @RequestMapping(value = "/exchangeRate", method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json; charset=utf-8"))
    fun readExchangeRate(): ResponseEntity<String> {
        logger.info("Received read exchange rate request.")
        val myName = adapter.sayMyName()
        return ResponseEntity.ok(myName)
    }
}