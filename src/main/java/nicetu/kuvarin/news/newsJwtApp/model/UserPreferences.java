package nicetu.kuvarin.news.newsJwtApp.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    @EmbeddedId
    private UserPreferencesId userPreferencesId;

    @ManyToOne
    @MapsId("user_id")
    private User user;

    @ManyToOne
    @MapsId("theme_id")
    private Theme theme;

    @Column(name="type")
    private boolean type; // false - не рекомендовать тему, true - рекомендовать
}
