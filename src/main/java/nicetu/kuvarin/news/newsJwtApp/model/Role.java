package nicetu.kuvarin.news.newsJwtApp.model;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name",length = 20)
    private ERole name;

    public Role(ERole name){
        this.name=name;
    }

    public Role() {

    }
}
