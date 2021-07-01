package freelance.home.comtrading.manager;

import freelance.home.comtrading.domain.item.Item;
import freelance.home.comtrading.domain.item.ParseStatus;
import freelance.home.comtrading.domain.request.RequestTask;
import freelance.home.comtrading.service.CacheService;
import freelance.home.comtrading.service.ItemService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class CacheProcessingManager {
    private static final Logger log = Logger.getLogger(CacheProcessingManager.class.getName());

    private final ItemService itemService;
    private final CacheService cacheService;

    public CacheProcessingManager(ItemService itemService, CacheService cacheService) {
        this.itemService = itemService;
        this.cacheService = cacheService;
    }

//    @Scheduled(fixedDelay = 500000, initialDelay = 100)
    public void findHotLineUrs() {

        long startTime = new Date().getTime();
        // 1. Получаем загруженные позиции
        Integer pageSize = 2000;
        for (int page = 85; true; page++) {
            System.out.println("PAGE: " + page);
            System.out.println("-----------------------------");
            List<RequestTask> allTasks = cacheService.getAllTasks(page, pageSize);
            if (allTasks.isEmpty()) break;
            System.out.println("STAGE_1: " + ((new Date().getTime() - startTime) / 1000));

            Set<Long> feedIds = allTasks.stream().map(t -> (long) t.getCacheId()).collect(Collectors.toSet());
            List<Item> items = itemService.getItemsByFeedIds(feedIds);
            System.out.println("STAGE_2: " + ((new Date().getTime() - startTime) / 1000));

            for (Item item : items) {
                try {
                    Optional<RequestTask> taskOptional = allTasks.stream().filter(t -> (long) t.getCacheId() == item.getFeedId()).findAny();
                    if (taskOptional.isPresent()) {
                        Document doc = Jsoup.parse(taskOptional.get().getHtml());
                        Element mainResult = doc.select("div a h3").first();
                        if (mainResult == null) {
                            item.setParseStatus(ParseStatus.NOT_FOUND);
                        } else {
                            String href = mainResult.parent().attr("href");
                            href = href.replaceAll(".*q=", "").replaceAll("/&sa.*", "");
                            if (!href.startsWith("https://hotline.ua/")) throw new Exception();

                            item.setAutoSearchUrl(href);
                            item.setParseStatus(mainResult.text().contains(item.getSku()) ? ParseStatus.GOOD : ParseStatus.BAD);
                        }
//                        Files.writeString(Paths.get("test.html"), task.getHtml());
//                        System.out.println();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    item.setParseStatus(ParseStatus.ERROR);
                }
            }
            System.out.println("STAGE_3: " + ((new Date().getTime() - startTime) / 1000));

            itemService.updateFeedItems(items);
            System.out.println("STAGE_4: " + ((new Date().getTime() - startTime) / 1000));
            System.out.println("========================");
        }

        System.exit(0);
    }
}