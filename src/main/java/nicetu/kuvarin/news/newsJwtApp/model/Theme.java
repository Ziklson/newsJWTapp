package nicetu.kuvarin.news.newsJwtApp.model;


import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="theme")
@Data
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @Size(max=255)
    private String name;

    public Theme() {
    }

    public Theme(String name) {
        this.name = name;
    }
}
