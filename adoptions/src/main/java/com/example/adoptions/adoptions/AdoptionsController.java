package com.example.adoptions.adoptions;

// reduce the blast radius of change

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.registry.ImportHttpServices;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


// two concerns: authentixation (who is it?), authorization (what rights?)
// password4j (Argon)
// Bouncycastle (Argon)

@EnableMultiFactorAuthentication(authorities =  {
        FactorGrantedAuthority.PASSWORD_AUTHORITY,
        FactorGrantedAuthority.OTT_AUTHORITY
})
@Configuration
class SecurityConfiguration {
//
//    @Bean
//    StandardPasswordEncoder passwordEncoderOld (){
//        return new StandardPasswordEncoder ();
//    }

//    select '{sha256}' || password from users


    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        var u = new JdbcUserDetailsManager(dataSource);
        u.setEnableUpdatePassword(true);
        return u;
    }

    @Bean
    Customizer<HttpSecurity> securityFilterChain() {
        return http -> http
                .webAuthn( a -> a
                        .allowedOrigins("http://localhost:8080")
                        .rpName("bootiful")
                        .rpId("localhost")
                )
                .oneTimeTokenLogin(ott -> ott
                        .tokenGenerationSuccessHandler((_, response, oneTimeToken) -> {
                            // sendgrid for email (SaaS)
                            // twilio (for sms, voice mail, etc.) (SaaS)

                            response.getWriter().println("you've got console mail!");
                            response.setContentType(MediaType.TEXT_PLAIN.toString());

                            IO.println("please go to http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue());
                        }));
    }


}


record Owner(String name) {
}

// new in 7!
@ImportHttpServices(value = CatFactsClient.class)
//
@Controller
@ResponseBody
@Transactional
class AdoptionsController {

    private final DogRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    AdoptionsController(DogRepository repository, ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @BatchMapping
    Map<Dog, Owner> owner(Collection<Dog> dogs) {
        IO.println("returning owners for " + dogs);
        var map = new HashMap<Dog, Owner>();
        for (var d : dogs)
            map.put(d, new Owner(d.name() + "' Owner"));
        return map;
    }
//
//    @SchemaMapping
//    Owner owner(Dog dog) {
//        // todo call http service for dog
//        // EEK! N+1
//        IO.println("returning the owner for dog " + dog);
//        return new Owner(dog.name() + "' Owner");
//    }


    @QueryMapping
    Collection<Dog> dogs() {
        return repository.findAll();
    }

    // v1
    @GetMapping(value = "/dogs", version = "1.0")
    Collection<Dog> dogsHttp() {
        return repository.findAll();
    }

    //  v2
    @GetMapping(value = "/dogs", version = "1.1")
    Collection<Map<String, Object>> dogsHttp2() {
        return this.repository.findAll()
                .stream()
                .map(dog -> Map.of("id", (Object) dog.id(), "dogName", dog.name()))
                .toList();
    }


    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var newDog = this.repository.save(new Dog(dog.id(), dog.name(), owner, dog.description()));
            IO.println("adopted " + newDog);
            this.applicationEventPublisher.publishEvent(new DogAdoptedEvent(dogId));
        });
    }

}

record CatFacts(Collection<CatFact> facts) {
}

record CatFact(String fact) {
}

// 6 (not 7!)

interface CatFactsClient {

    @GetExchange("https://www.catfacts.net/api")
    CatFacts facts();
}

//@Component
//class CatFactsClient {
//
//    private final RestClient http;
//
//    CatFactsClient(RestClient.Builder http) {
//        this.http = http.build();
//    }
//
//    CatFacts catFacts() {
//        return this.http.get()
//                .uri("https://www.catfacts.net/api")
//                .retrieve()
//                .body(CatFacts.class);
//    }
//}

@EnableResilientMethods
@ResponseBody
@Controller
class CatFactsController {

    private final CatFactsClient facts;

    CatFactsController(CatFactsClient facts) {
        this.facts = facts;
    }

    private final AtomicInteger counter = new AtomicInteger(0);

    // spring retry

    @ConcurrencyLimit(10)
    @Retryable(maxRetries = 5, includes = IllegalStateException.class)
    @GetMapping("/facts")
    CatFacts facts() {
        for (var i = 0; i < 4; i++) {
            if (counter.incrementAndGet() < 4) {
                IO.println("retrying " + counter.get());
                throw new IllegalStateException("simulated error");
            }
        }
        IO.println("got facts " + counter.get());
        return facts.facts();
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}

@Controller
@ResponseBody
class MeController {

    @GetMapping("/me")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}

//@Component
class MyClient {

    private final HttpGraphQlClient httpGraphQlClient =
            HttpGraphQlClient.builder().url("http://localhost:8080/graphql").build();


    @EventListener
    void run(ApplicationReadyEvent readyEvent) throws Exception {
        Assert.notNull(this.httpGraphQlClient, "the client shouldn't be null");

        var listOfDogs = this.httpGraphQlClient
                .document("""
                        query { dogs { id name } }
                        """)
                .retrieve("dogs")
                .toEntity(new ParameterizedTypeReference<List<Dog>>() {
                })
                .block();
        IO.println(listOfDogs);
    }

}