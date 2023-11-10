package nicetu.kuvarin.news.newsJwtApp.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="posts")
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Column(name="header")
    @Size(max=255)
    private String header;


    @NotBlank
    @Column(name="text",length = 10000)
    @Size(max=10000)
    private String text;


    @NotBlank
    @Column(name="date_published")
    private Date date;


    @NotBlank
    @Column(name="photo")
    @Size(max=255)
    private String photo;



    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(  name = "post_themes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "theme_id"))
    private Set<Theme> themes = new HashSet<>();


    @NotBlank
    @Column(name="likes_count")
    private Long likes;



    @JsonIgnore
    @NotBlank
    @ManyToOne(fetch = FetchType.LAZY)
    private User admin;



    public Post(String header, String text, Date date, String photo, Long likes, User admin) {
        this.header = header;
        this.text = text;
        this.date = date;
        this.photo = photo;
        this.likes = likes;
        this.admin = admin;
    }

    public Post(){

    }


}
