package dev.dead.reactivespringmongodb

import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Flux

@Document
data class Customer(
    @Id val id: String? = null,
    val name: String,
)
interface CustomerRepository : ReactiveCrudRepository<Customer, String>
@RestController
class CustomerRestController(val customerRepository: CustomerRepository) {
    @GetMapping("/customers")
    fun findAll(): Flux<Customer> = customerRepository.findAll()
}

class testFunctionalPara {
    fun work() {
        val upString = process("Test")
        { this.uppercase() }
        println(upString)
    }

    fun process(str: String, callback: String.() -> String): String = callback(str)
}

@SpringBootApplication
class ReactiveSpringMongodbApplication {
    @Bean
    fun routes(customerRepository: CustomerRepository) = coRouter {
        GET("/customers") {
            ServerResponse.ok()
                .bodyAndAwait(
                    customerRepository
                        .findAll().asFlow()
                )
        }
        // 3. POST (Create)
        POST("/customers") { request ->
            // Deserialize the request body into a Customer object
            val customer = request.awaitBody<Customer>()

            // Save the new customer and await the result
            val savedCustomer = customerRepository.save(customer).awaitFirst()

            // Return the created customer with status 201 Created
            ServerResponse.status(HttpStatus.CREATED)
                .bodyValueAndAwait(savedCustomer)
        }

        // 4. PUT (Update) by ID
        PUT("/{id}") { request ->
            val id: String = request.pathVariable("id")

            // Ensure the customer to update exists
            val existingCustomer = customerRepository.findById(id).awaitFirstOrNull()
                ?: return@PUT ServerResponse.status(HttpStatus.NOT_FOUND).buildAndAwait()

            // Deserialize the request body
            val updatedData = request.awaitBody<Customer>()

            // Copy the existing customer, preserving the original ID, and updating the name
            val customerToSave = existingCustomer.copy(
                name = updatedData.name
            )

            val updatedCustomer = customerRepository.save(customerToSave).awaitFirst()

            // Return the updated customer with status 200 OK
            ServerResponse.ok().bodyValueAndAwait(updatedCustomer)
        }

        // 5. DELETE by ID
        DELETE("/{id}") { request ->
            val id: String = request.pathVariable("id")

            val exists = customerRepository.existsById(id).awaitFirst()
            if (exists) {
                customerRepository.deleteById(id).awaitFirstOrNull()
                ServerResponse.noContent().buildAndAwait()
            } else {
                ServerResponse.status(HttpStatus.NOT_FOUND).buildAndAwait()
            }
        }
    }
}


@Bean
fun gateway(rlb: RouteLocatorBuilder) =
    rlb.routes {
        route {
            host("*.spring.io") and path("/proxy")
            filters {
                setPath("/guides")
                addRequestHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            }
            uri("https://spring.io")
        }
    }
    //java way
//    @Bean
//    fun routes(customerRepository: CustomerRepository): RouterFunction<ServerResponse?> {
//
//            return route()
//                .GET("/customers", HandlerFunction<ServerResponse> {
//                    ServerResponse.ok().body(customerRepository.findAll())
//                })
//                .build();
//        }

fun main(args: Array<String>) {
    runApplication<ReactiveSpringMongodbApplication>(*args)
}
