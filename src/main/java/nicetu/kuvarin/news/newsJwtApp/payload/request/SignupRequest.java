package nicetu.kuvarin.news.newsJwtApp.payload.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SignupRequest {

    @NotBlank
    @Size(max = 255)
    @Email
    private String email;


    @NotBlank
    @Size(min = 8, max = 255)
    private String password;


    @NotBlank
    @Size(max =100)
    private String first_name;


    @NotBlank
    @Size(max = 100)
    private String last_name;
}
