package nicetu.kuvarin.news.newsJwtApp.repository;

import nicetu.kuvarin.news.newsJwtApp.model.Comment;
import nicetu.kuvarin.news.newsJwtApp.model.CommentId;
import nicetu.kuvarin.news.newsJwtApp.model.Post;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, CommentId> {

    public List<Comment> findByIdPostIdOrderByIdDateDesc(Long id);

    public Optional<Comment> findById(CommentId id);

    public List<Comment> findByPost(Post post);



}
