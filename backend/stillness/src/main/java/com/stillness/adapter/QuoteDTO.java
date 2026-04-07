package com.stillness.adapter;

public class QuoteDTO {

    private final String text;
    private final String author;
    private final String source;

    public QuoteDTO(String text, String author, String source) {
        this.text = text;
        this.author = author;
        this.source = source;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public String getSource() {
        return source;
    }
}