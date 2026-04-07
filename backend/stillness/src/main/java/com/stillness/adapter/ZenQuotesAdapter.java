package com.stillness.adapter;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ZenQuotesAdapter {

    private static final String ZENQUOTES_URL = "https://zenquotes.io/api/random";

    private final RestTemplate restTemplate;

    public ZenQuotesAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public QuoteDTO fetchQuote() {
        ZenQuotesResponse[] responses = restTemplate.getForObject(ZENQUOTES_URL, ZenQuotesResponse[].class);

        if (responses == null || responses.length == 0) {
            throw new IllegalStateException("No quote received from ZenQuotes API");
        }

        ZenQuotesResponse raw = responses[0];
        return new QuoteDTO(raw.getQuote(), raw.getAuthor(), "zenquotes");
    }
}