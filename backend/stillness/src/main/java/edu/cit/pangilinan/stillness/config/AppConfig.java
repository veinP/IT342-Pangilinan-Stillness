package edu.cit.pangilinan.stillness.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Stripe stripeInitializer() {
        Stripe.apiKey = stripeApiKey;
        return new Stripe();
    }
}