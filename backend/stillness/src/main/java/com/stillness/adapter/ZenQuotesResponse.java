package com.stillness.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZenQuotesResponse {

    @JsonProperty("q")
    private String quote;

    @JsonProperty("a")
    private String author;

    public ZenQuotesResponse() {
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}