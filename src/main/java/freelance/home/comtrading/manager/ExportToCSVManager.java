package freelance.home.comtrading.manager;

import freelance.home.comtrading.domain.item.Item;
import freelance.home.comtrading.service.CacheService;
import freelance.home.comtrading.service.ItemService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ExportToCSVManager {
    private static final Logger log = Logger.getLogger(ExportToCSVManager.class.getName());

    private final ItemService itemService;
    private final CacheService cacheService;

    public ExportToCSVManager(ItemService itemService, CacheService cacheService) {
        this.itemService = itemService;
        this.cacheService = cacheService;
    }

//    @Scheduled(fixedDelay = 500000000, initialDelay = 100)
    public void findHotLineUrs() throws IOException {
        Integer batchSize = 5000;
        for (int page = 0; true; page++) {
            List<Item> items = itemService.getAllItems(page, batchSize);
            if (items.isEmpty()) break;

            List<String> allLines = new ArrayList<>();
            for (Item item : items) {
                String line = String.format(
                        "%s\t%s\t%s\t%s\t%s\t%s\t%s",
                        item.getFeedId(), item.getSku(), item.getName(),
                        item.getCategoryName(), item.getSuplPrice(),
                        item.getParseStatus().name(), item.getAutoSearchUrl()
                );

                line = line.replace("UNKNOWN","НЕ_ОБРАБОТАНО");
                line = line.replace("CONFIRMED","ПОДТВЕРЖДЕНО");
                line = line.replace("GOOD","ТОЧНОЕ_СОВПАДЕНИЕ");
                line = line.replace("BAD","ЧАСТИЧНОЕ_СОВПАДЕНИЕ");
                line = line.replace("NOT_FOUND","НЕ_НАЙДЕНО");
                line = line.replace("ERROR","ОШИБКА_ОБРАБОТКИ");
                allLines.add(line);
            }

            Files.writeString(Paths.get("feed.csv"), String.join("\n", allLines), StandardOpenOption.APPEND);
        }

        System.exit(0);
    }
}
