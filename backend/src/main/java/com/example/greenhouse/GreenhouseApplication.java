package com.example.greenhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point for the greenhouse management API. */
@SpringBootApplication
@EnableScheduling
public class GreenhouseApplication {
  public static void main(String[] args) {
    SpringApplication.run(GreenhouseApplication.class, args);
  }
}
