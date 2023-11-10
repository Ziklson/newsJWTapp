package nicetu.kuvarin.news.newsJwtApp.payload.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoveFromPreferencesRequest {

    @NotBlank
    String name;
}
