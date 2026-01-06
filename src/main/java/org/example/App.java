package org.example;

import org.example.dao.MovieDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class that launches the Movies REST API.
 */
@SpringBootApplication
public class App {

    @Autowired
    MovieDao movieDao;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
