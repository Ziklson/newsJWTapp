package nicetu.kuvarin.news.newsJwtApp.payload.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {

    @NotBlank
    private String email;

    @NotBlank
    private Date date;

    @NotBlank
    private String text;

}
