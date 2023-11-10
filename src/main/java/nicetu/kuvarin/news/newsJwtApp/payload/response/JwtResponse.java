package nicetu.kuvarin.news.newsJwtApp.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private List<String> roles;

    public JwtResponse(String token, Long id, String first_name, String last_name, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.roles = roles;
    }
}
