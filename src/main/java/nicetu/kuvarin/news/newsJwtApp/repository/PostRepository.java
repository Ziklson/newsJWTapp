package nicetu.kuvarin.news.newsJwtApp.repository;

import nicetu.kuvarin.news.newsJwtApp.model.Post;
import nicetu.kuvarin.news.newsJwtApp.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import java.util.*;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    public List<Post> findAllByDateAfter(Date date);

    public List<Post> findDistinctByIdIn(List<Long> ids);


    public List<Post> findAllByDateAfterAndThemesNotIn(Date date, Set<Theme> themes);


    public Post findByDateBetween(Date date1,Date date2);

    public Optional<Post> getPostById(Long id);



    @Query(value = "SELECT post_id " +
            "FROM post_themes pt JOIN posts p ON pt.post_id=p.id " +
            "WHERE pt.theme_id NOT IN (:unfavoriteThemes)" +
            "AND " +
            "pt.theme_id IN (:favoriteThemes)" +
            "AND " +
            "p.date_published > (:date_p)" +
            "GROUP BY (pt.post_id)" +
            "ORDER BY COUNT(pt.theme_id) DESC ",nativeQuery = true)
    public List<Long> findRecommendedPosts(@Param("unfavoriteThemes") Set<Long> unfavoriteThemes, @Param("favoriteThemes") Set<Long> favoriteThemes, @Param("date_p") Date date_p);


}
