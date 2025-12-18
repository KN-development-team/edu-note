package com.edu.edu_note;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EduNoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduNoteApplication.class, args);
	}

}
