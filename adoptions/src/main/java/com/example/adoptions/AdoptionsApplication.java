package com.example.adoptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class AdoptionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdoptionsApplication.class, args);
	}

}

//@Component
class YouIncompleteMeEventListener {

	private final IncompleteEventPublications publications;

    YouIncompleteMeEventListener(IncompleteEventPublications publications) {
        this.publications = publications;
    }

//    @Scheduled (cron = "* * * * ")
	void replay () {
//		this.publications.resubmitIncompletePublications(e -> e.);
	}

}