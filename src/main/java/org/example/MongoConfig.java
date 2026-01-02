package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Value("${mongo.password}")
    private String password;

    @Bean
    public MongoClient mongoClient() {
        // TODO Read all options from config
        String uri = "mongodb+srv://rdom:" + password +
                "@klusteri.mzbazgx.mongodb.net/?appName=Klusteri";

        return MongoClients.create(uri);
    }
}

