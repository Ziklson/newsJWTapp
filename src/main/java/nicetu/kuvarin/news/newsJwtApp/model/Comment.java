package nicetu.kuvarin.news.newsJwtApp.model;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="comments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @EmbeddedId
    private CommentId id;

    @ManyToOne
    @MapsId("user_id")
    private User user;

    @ManyToOne
    @MapsId("post_id")
    private Post post;


    @Column(name="text")
    @Size(max=1000)
    private String text;


}
