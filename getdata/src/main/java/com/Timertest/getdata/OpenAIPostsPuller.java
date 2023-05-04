package com.Timertest.getdata;

import org.apache.camel.builder.RouteBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;

public class OpenAIPostsPuller extends RouteBuilder {
    private static final String OPENAI_RESEARCH_URL = "https://openai.com/research";

    @Override
    public void configure() {

        from ("timer://pullLatestPosts?fixedRate=true&period=300000")

                .process(exchange -> {
                    Document doc = Jsoup.connect(OPENAI_RESEARCH_URL).get();
                    Elements posts = doc.getElementsByClass("cols-container relative");
                    for (org.jsoup.nodes.Element post : posts) {
                        String title = post.select("h2").text();
                        String abstractText = post.select(".post-preview__description").text();
                        String authors = post.select(".post-preview__authors").text();
                        String[] authorNames = authors.split(",");
                        StringBuilder fileContent = new StringBuilder(abstractText + "\n\n" + authors + "\n\n");
                        for (String author : authorNames) {
                            String googleSearchUrl = "https://www.google.com/search?q=" + author.trim().replace(" ", "+");
                            Document searchResults = Jsoup.connect(googleSearchUrl).get();
                            Elements links = searchResults.select(".r>a");
                            int linkCount = 0;
                            for (int j = 0; j < links.size() && linkCount < 5; j++) {
                                String link = links.get(j).absUrl("href");
                                if (link.startsWith("http")) {
                                    fileContent.append(link).append("\n");
                                    linkCount++;
                                }
                            }
                        }
                        try (FileWriter writer = new FileWriter(title + ".txt")) {
                            writer.write(fileContent.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }
}
