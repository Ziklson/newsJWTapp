package nicetu.kuvarin.news.newsJwtApp.payload.request;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Set;

@Data
public class EditPostRequest {

    private String header;

    private String text;

    private String photo;

    private Set<String> themes;

}
