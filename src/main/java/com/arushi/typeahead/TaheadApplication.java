package com.arushi.typeahead;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaheadApplication {

	public static void main(String[] args) {

		SpringApplication.run(TaheadApplication.class, args);
		System.out.println("Hello Typeahead");
	}

}
