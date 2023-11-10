package nicetu.kuvarin.news.newsJwtApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Date;

@Embeddable
@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentId implements Serializable {


    @Column(name="user_id")
    private Long userId;

    @Column(name="post_id")
    private Long postId;

    @Column(name="date_published")
    private Date date;

}
