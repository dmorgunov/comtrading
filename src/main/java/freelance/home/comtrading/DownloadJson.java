package freelance.home.comtrading;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DownloadJson {
    private static final XmlMapper mapper = new XmlMapper();

    public static void main(String[] args) throws IOException {
        String jsonFeed = Jsoup.connect("https://api2.comtrading.ua/feed")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .maxBodySize(0)
                .execute().body();
        Files.writeString(Paths.get("feed.json"), jsonFeed);
        System.exit(0);

//        String jsonFeed = Files.readString(Paths.get("feed_all.json"));
        jsonFeed = jsonFeed.replaceAll("&\\w+", "");

        JsonNode mainNode = mapper.readTree(jsonFeed);

        HashMap<Long, String> filledItems = new HashMap<>();
        for (JsonNode node : mainNode.get("items").get("item")) {
            long id = node.get("id").asLong();
            String hotlineUrl = node.get("hotline_url").asText();

            if (hotlineUrl.startsWith("https"))
                filledItems.put(id, hotlineUrl);
        }

        Files.writeString(Paths.get("feed_all.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(filledItems));
        System.out.println();
    }
}
