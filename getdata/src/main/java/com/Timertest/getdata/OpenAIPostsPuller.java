package com.Timertest.getdata;

import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenAIPostsPuller extends RouteBuilder {

        private static final String OPENAI_RESEARCH_URL = "https://openai.com/research";

        @Override
        public void configure() {

            from ("timer://pullLatestPosts?fixedRate=true&period=300000")
                    .process(exchange -> {
                    Document doc = Jsoup.connect(OPENAI_RESEARCH_URL).get();
                    Elements links = doc.select("div.cols-container.relative a[href]:not([target])");
                        int count = 0;
                    for (Element link : links) {
                        try {
                            String href = link.attr("abs:href");
                            Desktop.getDesktop().browse(new URI(href));
                            Document postDoc = Jsoup.connect(href).get();
                            Elements aText = postDoc.select("div.cols-container h2.f-heading-3");
                            String abText;
                            abText = aText.text();
                            if (abText != null && abText.equalsIgnoreCase("Abstract")) {
                                String postTitle = postDoc.title();
                                String fileTitle = URLEncoder.encode(postTitle, StandardCharsets.UTF_8.toString());
                                Elements paragraphs1 = postDoc.select("div.cols-container p");
                                String abstractText = null;
                                if (paragraphs1 != null && paragraphs1.get(0) != null) {
                                    abstractText = paragraphs1.get(0).childNodes().get(0).toString();
                                    System.out.println("AbstractText: " + abstractText);

                                File file = new File(fileTitle);
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                if (abstractText != null) {
                                    writer.write(abstractText);
                                    writer.newLine();
                                    writer.write("\r\n");
                                }

                                Elements paragraphs = postDoc.select("div.cols-container div.f-body-1.ui-richtext p");


                                for (Element paragraph : paragraphs) {

                                    String text = paragraph.html().replaceAll("(?i)<br[^>]*>", "\n");
                                    String[] lines = text.split("\n");

                                    for (String line : lines) {
                                        writer.write("\r\n");
                                        writer.write(line);
                                        writer.newLine();
                                        writer.write("\r\n");
                                        // Encode search query
                                        String query = URLEncoder.encode(line, StandardCharsets.UTF_8.toString());
                                        // Construct Google search URL
                                        String googleSearchUrl = "https://www.google.com/search?q=" + query;
                                        Document searchDoc = Jsoup.connect(googleSearchUrl)
                                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                                                .get();
                                        // Get first five search result links
                                        Elements searchLinks = searchDoc.select("div#search div.v7W49e a[href]");
                                        List<String> urls = new ArrayList<>();
                                        for (Element indurl : searchLinks) {
                                            String hreff = indurl.attr("href");
                                            // Remove tracking parameters from link URL
                                            hreff = hreff.replaceAll("&ved=.*", "").replaceAll("&sa=.*", "");
                                            // Add link to list
                                            urls.add(hreff);
                                            // Stop when five links are found
                                            if (urls.size() == 5) {
                                                break;
                                            }
                                        }
                                        // Print link
                                        for (String searchlink : urls) {

                                            writer.write(searchlink);
                                            writer.newLine();
                                        }
                                    }

                                }
                                writer.close();
                                count++;

                            }
                        }
                            else
                            {
                                count ++;}
                        }


                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (count == 5) {
                            break;
                        }
                    }



                });


    }
}

