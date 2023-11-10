package nicetu.kuvarin.news.newsJwtApp.mapper;


import nicetu.kuvarin.news.newsJwtApp.model.Post;
import nicetu.kuvarin.news.newsJwtApp.payload.response.PostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostMapper {

    @Autowired
    ThemeMapper themeMapper;

    public List<PostResponse> toPostResponseList(List<Post> posts){
        List<PostResponse> result=new ArrayList<>();
        for(Post post: posts){
            result.add(new PostResponse(post.getHeader(),
                    post.getText(),
                    post.getPhoto(),
                    post.getDate(),
                    themeMapper.toStringThemesList(post.getThemes()),
                    post.getLikes()));
        }
        return result;
    }
}
