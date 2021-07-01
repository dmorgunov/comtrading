package freelance.home.comtrading.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "request")
@NoArgsConstructor
@AllArgsConstructor
public @Data class RequestTask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private Integer cacheId;

    private String taskId;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String html = "";
    private String params = "";

    private ReqTaskType type;
    // --------------------------------

    public RequestTask(String taskId, String url) {
        this.taskId = taskId;
        this.url = url;

        this.type = ReqTaskType.ITEM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestTask that = (RequestTask) o;
        return taskId.equals(that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    public void updateCacheId() {
        this.cacheId = Integer.parseInt(taskId);
    }
}