package nicetu.kuvarin.news.newsJwtApp.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
}
