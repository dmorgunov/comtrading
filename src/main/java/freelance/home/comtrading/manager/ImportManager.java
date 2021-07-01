package freelance.home.comtrading.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import freelance.home.comtrading.domain.item.Item;
import freelance.home.comtrading.service.ItemService;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ImportManager {
    private ObjectMapper jsonMapper = new ObjectMapper();
    private XmlMapper xmlMapper = new XmlMapper();

    private final ItemService itemService;

    public ImportManager(ItemService itemService) {
        this.itemService = itemService;
    }

//    @Scheduled(fixedDelay = 500000000, initialDelay = 100)
    public void importFeed() throws IOException {
        System.out.println("--------------------------------");
        JsonNode jsonFeedNode = jsonMapper.readTree(
                Jsoup.connect("https://api2.comtrading.ua/feed")
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .maxBodySize(0)
                        .execute().body()
        );

        JsonNode approvedFeed = xmlMapper.readTree(
                Jsoup.connect("http://api2.comtrading.ua/feed_hotline_all")
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .maxBodySize(0)
                        .execute().body()
                        .replaceAll("&\\w+", "")
        );

        // Разбираем одобренный фид
        HashMap<Long, String> approvedItems = new HashMap<>();
        for (JsonNode node : approvedFeed.get("items").get("item")) {
            long id = node.get("id").asLong();
            String hotlineUrl = node.get("hotline_url").asText();

            if (hotlineUrl.startsWith("https"))
                approvedItems.put(id, hotlineUrl);
        }

        // Добавляем весь фид
        List<Item> items = new ArrayList<>();
        for (JsonNode feedNode : jsonFeedNode) {
            Item item = new Item();

            item.setFeedId(feedNode.get("id").asLong());
            item.setModel(feedNode.get("model").asText());
            item.setSku(feedNode.get("sku").asText());
            item.setName(feedNode.get("name").asText());
            item.setCategoryId(feedNode.get("category_id").asLong());
            item.setCategoryName(feedNode.get("category_name").asText());
            item.setSuplPrice(feedNode.get("supl_price").asDouble());
            item.setPrice(feedNode.get("price").asDouble());
//            item.setApprovedUrl(approvedItems.get(item.getFeedId()));

            items.add(item);
        }

        // 3. Сохраняем позиции в базу
        itemService.updateFeedItems(items);
        System.exit(0);
    }
}
