package com.mandiri.mandiriintern;

public class Article {
    private Source source;

    private String title;

    private String url;
    private String urlToImage;


    // Nested Source class
    public static class Source {
        private String id;
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
    }

    // Getters
    public Source getSource() { return source; }

    public String getTitle() { return title; }

    public String getUrl() { return url; }
    public String getUrlToImage() { return urlToImage; }

}
