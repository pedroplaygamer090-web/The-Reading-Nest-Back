package com.br.The_Reading_Nest_Back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TheReadingNestBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheReadingNestBackApplication.class, args);

		var encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
		System.out.println("HASH ADMIN: " + encoder.encode("Admin123!"));
		System.out.println("HASH CLIENTE: " + encoder.encode("Cliente123!"));

	}

}
