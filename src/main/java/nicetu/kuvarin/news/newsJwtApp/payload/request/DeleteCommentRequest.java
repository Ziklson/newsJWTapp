package nicetu.kuvarin.news.newsJwtApp.payload.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteCommentRequest {

    @NotBlank
    String username;

    @NotBlank
    Date date;
}
