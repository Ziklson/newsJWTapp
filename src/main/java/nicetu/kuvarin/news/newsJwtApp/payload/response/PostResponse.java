package nicetu.kuvarin.news.newsJwtApp.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import nicetu.kuvarin.news.newsJwtApp.model.Comment;
import nicetu.kuvarin.news.newsJwtApp.model.Post;
import nicetu.kuvarin.news.newsJwtApp.model.Theme;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class PostResponse {

//    @NotBlank
//    private Post post;

    @NotBlank
    private String header;

    @NotBlank
    private String text;

    @NotBlank
    private String photo;

    @NotBlank
    private Date date;

    @NotBlank
    private List<String> themes;

    @NotBlank
    private Long likes;





}
