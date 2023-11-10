package nicetu.kuvarin.news.newsJwtApp.service;

import nicetu.kuvarin.news.newsJwtApp.exception.CommentNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.CreateCommentException;
import nicetu.kuvarin.news.newsJwtApp.exception.PostNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserNotAuthorizedException;
import nicetu.kuvarin.news.newsJwtApp.mapper.PostMapper;
import nicetu.kuvarin.news.newsJwtApp.model.*;
import nicetu.kuvarin.news.newsJwtApp.payload.request.CreateCommentRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.DeleteCommentRequest;
import nicetu.kuvarin.news.newsJwtApp.repository.*;
import nicetu.kuvarin.news.newsJwtApp.security.jwt.JwtUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class CommentServiceTest {
    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    PostMapper postMapper;
    @Autowired
    UserPreferencesRepository userPreferencesRepository;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PostService postService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    CommentService commentService;

    @Before
    public void check(){
        userPreferencesRepository.deleteAll();
        userRepository.deleteAll();
        postRepository.deleteAll();

        themeRepository.deleteAll();
        roleRepository.deleteAll();

        List<Role> roles= new ArrayList<>();
        roles.add(new Role(ERole.ROLE_ADMIN));
        roles.add(new Role(ERole.ROLE_USER));
        roleRepository.saveAllAndFlush(roles);
    }

    @Test
    public void getComments_ShouldReturnCommentsByPostId() throws CommentNotFoundException {
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);

        Comment comment1=createComment(user,post,"text1");
        Comment comment2=createComment(user,post,"text2");
        Comment comment3=createComment(user,post,"text3");

        List<Comment> comments=new ArrayList<>();

        comments.add(comment3);
        comments.add(comment2);
        comments.add(comment1);

        List<Comment> result=commentService.getComments(String.valueOf(post.getId()));

        Assert.assertEquals(comments,result);



    }


    @Test
    public void getComments_ShouldThrowNoCommentsExceptionIfPostHasNoComments(){
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));

        try{
            List<Comment> comments=commentService.getComments(String.valueOf(post.getId()));
        } catch (CommentNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        }
    }

    @Test
    public void createComment_ShouldCreateCommentFromUserOnPostWithId() throws UserNotAuthorizedException, PostNotFoundException {
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));

        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);

        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));


        List<Comment> comments=commentRepository.findByPost(post);

        Assert.assertTrue(comments.isEmpty());
        String commentText="newComment";

        Date dateBefore=new Date();
        commentService.createComment(String.valueOf(post.getId()),new CreateCommentRequest(commentText));
        Date dateAfter=new Date();

        List<Comment> commentsResult=commentRepository.findByPost(post);

        Assert.assertFalse(commentsResult.isEmpty());
        Assert.assertEquals(commentText,commentsResult.get(0).getText());
        Assert.assertEquals(user,commentsResult.get(0).getUser());

        Assert.assertTrue(commentsResult.get(0).getId().getDate().getTime() > dateBefore.getTime() && commentsResult.get(0).getId().getDate().getTime() < dateAfter.getTime());

    }


    @Test
    public void createComment_ShouldThrowExceptionIfUserNotAuth(){
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));
        try{
            commentService.createComment(String.valueOf(post.getId()),new CreateCommentRequest("commentText"));
        } catch (UserNotAuthorizedException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        } catch (PostNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    public void createComment_ShouldThrowExceptionIfPostNotFound(){
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        try{
            commentService.createComment(String.valueOf(231412),new CreateCommentRequest("commentText"));
        } catch (UserNotAuthorizedException e) {
            throw new RuntimeException(e);
        } catch (PostNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        }

    }


    @Test
    public void deleteComment_ShouldDeleteCommentByCommentId() throws CommentNotFoundException {
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        Comment comment=createComment(user,post,"text");
        DeleteCommentRequest deleteCommentRequest=new DeleteCommentRequest(comment.getUser().getEmail(),comment.getId().getDate());
        commentService.deleteComment(String.valueOf(post.getId()),deleteCommentRequest);
        Assert.assertFalse(commentRepository.existsById(comment.getId()));

    }

    @Test
    public void deleteComment_ShouldThrowCommentNotFoundException(){
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        Comment comment=createComment(user,post,"text");
        DeleteCommentRequest deleteCommentRequest=new DeleteCommentRequest(comment.getUser().getEmail(),comment.getId().getDate());

        try {
            commentService.deleteComment(String.valueOf(post.getId()+514514),deleteCommentRequest);
        } catch (CommentNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        }
    }


    private Post createTestPost(String header, String text, Date date, String photo, Long likes, User admin, Set<Theme> themes) {
        Post post = new Post(header,text,date,photo,likes,admin);
        if(!themes.isEmpty())
            post.setThemes(themes);

        return postRepository.saveAndFlush(post);
    }
    private User createTestUser(String email,String first_name,String last_name,String password,boolean admin){
        User user=new User(email,first_name,last_name,encoder.encode(password));

        if(admin){
            Set<Role> roles= new HashSet<>();
            roles.add(roleRepository.findByName(ERole.ROLE_ADMIN).get());
            user.setRoles(roles);
        }

        return userRepository.saveAndFlush(user);
    }
    private Theme createTestTheme(String name){
        Theme theme = new Theme(name);
        return themeRepository.saveAndFlush(theme);
    }

    private Authentication doAuth(String email, String password){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        return authentication;
    }

    private Comment createComment(User user,Post post, String text){
        CommentId commentId=new CommentId(user.getId(),post.getId(),new Date());
        Comment comment=new Comment(commentId,user,post,text);
        return commentRepository.saveAndFlush(comment);
    }
}