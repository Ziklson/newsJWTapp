package nicetu.kuvarin.news.newsJwtApp.payload.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {

    @NotBlank
    private String header;

    @NotBlank
    private String text;

    @NotBlank
    private String photo;

    @NotBlank
    private Set<String> themes;

}
