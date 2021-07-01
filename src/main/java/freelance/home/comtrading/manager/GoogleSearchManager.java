package freelance.home.comtrading.manager;

import freelance.home.comtrading.domain.item.Item;
import freelance.home.comtrading.domain.item.Status;
import freelance.home.comtrading.domain.request.RequestTask;
import freelance.home.comtrading.service.ItemService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class GoogleSearchManager {
    private static final Logger log = Logger.getLogger(GoogleSearchManager.class.getName());

    private final ItemService itemService;
    private final RequestManager requestManager;

    public GoogleSearchManager(ItemService itemService, RequestManager requestManager) {
        this.itemService = itemService;
        this.requestManager = requestManager;
    }

//    @Scheduled(fixedDelay = 500, initialDelay = 100)
    public void findHotLineUrs() throws Exception {

        // 1. Получаем задачу на загрузку
        List<Item> items = itemService.getNotFilledItems(1024);
        if (items.size() < 500) System.exit(0);

        // 2. Формируем список запросов
        List<RequestTask> googleSearchRequests = items.stream().map(i ->
                new RequestTask(String.valueOf(i.getFeedId()),
//                        "https://www.google.com/search?q=" + encodeValue("%s %s \"%s\" site:hotline.ua".formatted(
                        "https://www.google.com/search?q=" + encodeValue("%s site:hotline.ua".formatted(
                                i.getSku()
                        )))).collect(Collectors.toList());

        // 3. Загружаем HTML странички Google выдачи
        googleSearchRequests = requestManager.execute(googleSearchRequests);
        Set<String> downloadedIds = googleSearchRequests.stream().map(RequestTask::getTaskId).collect(Collectors.toSet());
        items.removeIf(i->!downloadedIds.contains(String.valueOf(i.getFeedId())));

        items.forEach(i->i.setStatus(Status.SEARCH));
        itemService.updateFeedItems(items);
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
}
