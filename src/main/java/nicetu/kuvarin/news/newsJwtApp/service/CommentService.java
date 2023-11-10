package nicetu.kuvarin.news.newsJwtApp.service;


import nicetu.kuvarin.news.newsJwtApp.exception.CommentNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.CreateCommentException;
import nicetu.kuvarin.news.newsJwtApp.exception.PostNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserNotAuthorizedException;
import nicetu.kuvarin.news.newsJwtApp.mapper.CommentMapper;
import nicetu.kuvarin.news.newsJwtApp.model.Comment;
import nicetu.kuvarin.news.newsJwtApp.model.CommentId;
import nicetu.kuvarin.news.newsJwtApp.model.Post;
import nicetu.kuvarin.news.newsJwtApp.model.User;
import nicetu.kuvarin.news.newsJwtApp.payload.request.CreateCommentRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.DeleteCommentRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.response.CommentResponse;
import nicetu.kuvarin.news.newsJwtApp.repository.CommentRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.PostRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Comment> getComments(String id) throws CommentNotFoundException {
        List<Comment> result=commentRepository.findByIdPostIdOrderByIdDateDesc(Long.parseLong(id));
        if(result.isEmpty()){
            throw new CommentNotFoundException("Комментарии не найдены");
        }
        return result;
    }

    public List<CommentResponse> getCommentResponseList(List<Comment> comments){
        return commentMapper.toCommentResponseList(comments);
    }


    @Transactional
    public void createComment(String id, CreateCommentRequest createCommentRequest) throws PostNotFoundException, UserNotAuthorizedException {
        Comment comment=new Comment();


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Optional<User> userOpt=userRepository.findByEmail(userDetails.getUsername());
            Optional<Post> postOpt=postRepository.getPostById(Long.parseLong(id));
            if(userOpt.isPresent() && postOpt.isPresent()){
                User user = userOpt.get();
                Post post = postOpt.get();
                CommentId commentId = new CommentId();

                commentId.setUserId(user.getId());
                commentId.setPostId(post.getId());
                commentId.setDate(new Date());

                comment.setId(commentId);
                comment.setUser(user);
                comment.setPost(post);
                comment.setText(createCommentRequest.getText());
                commentRepository.save(comment);
            }
            else{
                throw new PostNotFoundException("Пост не найден");
            }
        }
        else{
            throw new UserNotAuthorizedException("Пользователь не авторизован");
        }






    }

    public void deleteComment(String id, DeleteCommentRequest deleteCommentRequest) throws CommentNotFoundException {

        Optional<User> userOpt=userRepository.findByEmail(deleteCommentRequest.getUsername());
        Optional<Post> postOpt=postRepository.getPostById(Long.parseLong(id));

        if(userOpt.isPresent() && postOpt.isPresent()){
            CommentId commentId=new CommentId();
            User user=userOpt.get();
            Post post=postOpt.get();
            commentId.setUserId(user.getId());
            commentId.setPostId(post.getId());
            commentId.setDate(deleteCommentRequest.getDate());
            Optional<Comment> commentOpt=commentRepository.findById(commentId);
            if(commentOpt.isPresent())
                commentRepository.delete(commentOpt.get());
            else
                throw new CommentNotFoundException("Комментарий не найден!");
        }
    }

}
