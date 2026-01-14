package com.example.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.repository.ListCrudRepository;


@SpringBootApplication
public class PersistenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersistenceApplication.class, args);
    }

    @Bean
    JdbcPostgresDialect jdbcPostgresDialect (){
        return JdbcPostgresDialect.INSTANCE ;
    }

    @Bean
    ApplicationRunner runner(DogRepository repository) {
        return a -> repository.findAll().forEach(IO::println);
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name) {
}