package nicetu.kuvarin.news.newsJwtApp.mapper;


import nicetu.kuvarin.news.newsJwtApp.model.Comment;
import nicetu.kuvarin.news.newsJwtApp.payload.response.CommentResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommentMapper {

    public List<CommentResponse> toCommentResponseList(List<Comment> comments){
        List<CommentResponse> result=new ArrayList<>();
        for(Comment comment: comments){
            result.add(new CommentResponse(comment.getUser().getEmail(),comment.getId().getDate(), comment.getText()));
        }
        return result;
    }
}
