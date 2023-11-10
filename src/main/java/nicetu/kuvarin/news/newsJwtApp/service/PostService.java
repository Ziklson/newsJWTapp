package nicetu.kuvarin.news.newsJwtApp.service;


import nicetu.kuvarin.news.newsJwtApp.exception.CreatePostException;
import nicetu.kuvarin.news.newsJwtApp.exception.PostNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.PostsNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserNotAuthorizedException;
import nicetu.kuvarin.news.newsJwtApp.mapper.PostMapper;
import nicetu.kuvarin.news.newsJwtApp.model.Post;
import nicetu.kuvarin.news.newsJwtApp.model.Theme;
import nicetu.kuvarin.news.newsJwtApp.model.User;
import nicetu.kuvarin.news.newsJwtApp.model.UserPreferences;
import nicetu.kuvarin.news.newsJwtApp.payload.request.CreatePostRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.EditPostRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.FindPostRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.response.PostResponse;
import nicetu.kuvarin.news.newsJwtApp.repository.PostRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.ThemeRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.UserPreferencesRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;


    public List<Post> getAll() throws PostsNotFoundException, UserNotAuthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Post> posts;
        Date date=new Date(new Date().getTime() - 86_400_000);
        if(authentication instanceof AnonymousAuthenticationToken || authentication == null){
            System.out.println("Пользователь не авторизован");
            posts=postRepository.findAllByDateAfter(date);
        }
        else{
            System.out.println("Пользователь авторизован");

            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Optional<User> userOpt= userRepository.findByEmail(userDetails.getUsername());
            User user;
            if(userOpt.isPresent())
                user=userOpt.get();
            else
                throw new UserNotAuthorizedException("Authorized user not found");

            Set<Long> unRecommendedThemes=new HashSet<>();
            Set<Long> recommendedThemes=new HashSet<>();
            List<UserPreferences> userPreferencesList=userPreferencesRepository.findAllByUser(user);
            for(UserPreferences userPreferences: userPreferencesList){
                if(userPreferences.isType()){
                    recommendedThemes.add(userPreferences.getTheme().getId());
                }
                else{
                    unRecommendedThemes.add(userPreferences.getTheme().getId());
                }
            }
            List<Long> postsIds=postRepository.findRecommendedPosts(unRecommendedThemes,recommendedThemes,date);
            posts = postRepository.findDistinctByIdIn(postsIds);
            List<Post> sortedPosts = new ArrayList<>(posts.size());
            for (Long id : postsIds) {
                for (Post post : posts) {
                    if (post.getId().equals(id)) {
                        sortedPosts.add(post);
                        break;
                    }
                }
            }
            if(sortedPosts.isEmpty()){
                sortedPosts=postRepository.findAllByDateAfterAndThemesNotIn(date,themeRepository.findAllByIdIn(unRecommendedThemes));
            }
            posts=sortedPosts;
        }
        if(posts.isEmpty()){
            throw new PostsNotFoundException("Посты не найдены");
        }
        return posts;
    }


    public Post getPostById(String id) throws PostNotFoundException {
        Long postId=Long.parseLong(id);
        Optional<Post> post= postRepository.findById(postId);
        if(post.isPresent())
            return post.get();
        else
            throw new PostNotFoundException("Пост c id: " + id + " не найден");

    }

    public void createPost(CreatePostRequest createPostRequest) throws CreatePostException {
        Post post=new Post();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null){
            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Optional<User> admin= userRepository.findByEmail(userDetails.getUsername());
            if(admin.isPresent()){
                post.setHeader(createPostRequest.getHeader());
                post.setText(createPostRequest.getText());
                post.setDate(new Date());
                post.setPhoto(createPostRequest.getPhoto());
                post.setLikes(0L);
                post.setThemes(themeRepository.findThemesByNameIn(createPostRequest.getThemes()));
                post.setAdmin(admin.get());
            }
        }
        else{
            throw new CreatePostException("Authorized administrator not found");
        }
        postRepository.save(post);
    }


    public void editPost(String id,EditPostRequest editPostRequest) throws PostsNotFoundException, UserNotAuthorizedException {
            Optional<Post> postOpt=postRepository.getPostById(Long.parseLong(id));
            if(postOpt.isPresent()) {
                Post post = postOpt.get();
                if (editPostRequest.getHeader() != null)
                    post.setHeader(editPostRequest.getHeader());
                if (editPostRequest.getText() != null)
                    post.setText(editPostRequest.getText());
                if (editPostRequest.getPhoto() != null)
                    post.setPhoto(editPostRequest.getPhoto());
                if (editPostRequest.getThemes() != null)
                    post.setThemes(themeRepository.findThemesByNameIn(editPostRequest.getThemes()));
                postRepository.save(post);
            }
            else{
                throw new PostsNotFoundException("Пост не найден");
            }
    }

    public void deletePost(String id) throws PostNotFoundException {
        Long postId=Long.parseLong(id);
        if(postRepository.existsById(postId))
            postRepository.deleteById(postId);
        else
            throw new PostNotFoundException("Пост с id: " + id + " не найден!");
    }


    public void setLike(String postId) throws PostNotFoundException, UserNotAuthorizedException {
        Optional<Post> postOpt = postRepository.getPostById(Long.parseLong(postId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null){
            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            Optional<User> userOpt= userRepository.findByEmail(userDetails.getUsername());

            if(postOpt.isPresent() && userOpt.isPresent()){
                Post post = postOpt.get();
                User user=userOpt.get();
                Long likes= post.getLikes();
                Set<Post> posts_set = new HashSet<>(user.getPosts());
                if(posts_set.contains(post)){
                    likes=likes-1;
                    posts_set.remove(post);
                }
                else{
                    likes=likes+1;
                    posts_set.add(post);
                }
                user.setPosts(posts_set);
                post.setLikes(likes);
                postRepository.save(post);
                userRepository.save(user);

            }
            else
                throw new PostNotFoundException("Пост с id: " + postId + " не найден");
        }
        else
            throw new UserNotAuthorizedException("Пользователь не авторизован");


    }



    public List<PostResponse> getPostResponseList(List<Post> posts){
        return postMapper.toPostResponseList(posts);
    }




}
