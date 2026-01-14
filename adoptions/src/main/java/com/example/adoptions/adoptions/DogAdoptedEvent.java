package com.example.adoptions.adoptions;

import org.springframework.modulith.events.Externalized;

//@Externalized ("integrationMessageChannel")
public record DogAdoptedEvent( int dogId) {
}
