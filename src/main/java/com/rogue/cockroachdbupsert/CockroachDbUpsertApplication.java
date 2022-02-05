package com.rogue.cockroachdbupsert;

import com.rogue.cockroachdbupsert.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CockroachDbUpsertApplication implements CommandLineRunner {

	@Autowired
	UserService userService;

	public static void main(String[] args) {
		SpringApplication.run(CockroachDbUpsertApplication.class, args);
	}

	@Override
	public void run(String... args) {
		userService.save();
		userService.save();
		userService.upsert();

		userService.saveAll(1);
		userService.upsertAll(1,false);
		userService.upsertAll(1,true);

		userService.saveAll(10);
		userService.upsertAll(10,false);
		userService.upsertAll(10,true);

		userService.saveAll(100);
		userService.upsertAll(100,false);
		userService.upsertAll(100,true);

		userService.saveAll(1000);
		userService.upsertAll(1000,false);
		userService.upsertAll(1000,true);

		userService.saveAll(2500);
		userService.upsertAll(2500,false);
		userService.upsertAll(2500,true);

		userService.saveAll(5000);
		userService.upsertAll(5000, false);
		userService.upsertAll(5000, true);

		userService.saveAll(10000);
		userService.upsertAll(10000,false);
		userService.upsertAll(10000,true);

		userService.saveAll(100000);
		userService.upsertAll(100000,false);
		userService.upsertAll(100000,true);

		userService.saveAll(1000000);
		userService.upsertAll(1000000,false);
		userService.upsertAll(1000000,true);
	}
}
