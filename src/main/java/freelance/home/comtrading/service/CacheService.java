package freelance.home.comtrading.service;

import freelance.home.comtrading.domain.request.RequestTask;
import freelance.home.comtrading.repository.CacheRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class CacheService {
    private final CacheRepository cacheRepository;

    public CacheService(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    // -------------------- Cache support methods --------------------
    public List<RequestTask> getAllTasks(Integer page, Integer size) {
        Page<RequestTask> all = cacheRepository.findAll(PageRequest.of(page, size));
        return all.getContent();
    }

    public void loadFromCache(List<RequestTask> tasks) {
        tasks.forEach(RequestTask::updateCacheId);

        for (int i = 0; i < tasks.size(); i++) {
            RequestTask task = tasks.get(i);

            if (cacheRepository.existsByCacheId(task.getCacheId()))
                tasks.set(i, cacheRepository.findFirstByCacheId(task.getCacheId()));
        }
    }

    public void saveToCache(HashSet<RequestTask> tasks) {
        tasks.forEach(RequestTask::updateCacheId);

        for (RequestTask task : tasks) {
            if (!cacheRepository.existsByCacheId(task.getCacheId()))
                cacheRepository.save(task);
        }
    }

    @Modifying
    @Transactional
    public void clearAllCache() {
        cacheRepository.truncate();
    }
}
