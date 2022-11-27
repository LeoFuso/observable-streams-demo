package io.github.leofuso.obs.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class ObservableStreamsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservableStreamsDemoApplication.class, args);
    }

}
