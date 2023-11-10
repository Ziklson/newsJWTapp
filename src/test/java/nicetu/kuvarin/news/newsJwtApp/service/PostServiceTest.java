package nicetu.kuvarin.news.newsJwtApp.service;

import nicetu.kuvarin.news.newsJwtApp.exception.CreatePostException;
import nicetu.kuvarin.news.newsJwtApp.exception.PostNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.PostsNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserNotAuthorizedException;
import nicetu.kuvarin.news.newsJwtApp.mapper.PostMapper;
import nicetu.kuvarin.news.newsJwtApp.model.*;
import nicetu.kuvarin.news.newsJwtApp.payload.request.CreatePostRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.EditPostRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.FindPostRequest;
import nicetu.kuvarin.news.newsJwtApp.repository.*;
import nicetu.kuvarin.news.newsJwtApp.security.jwt.JwtUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;



@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class PostServiceTest {
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
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PostService postService;

    @Autowired
    PasswordEncoder encoder;


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
    public void getAll_ShouldReturnAllPostAfterDateForUnAuthUser() throws UserNotAuthorizedException, PostsNotFoundException {
        Date date=new Date();

        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post1 = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme1")));
        Post post2 = createTestPost("header2","text2",new Date(),"photo2",(long) 0,admin, Collections.singleton(createTestTheme("testTheme2")));
        Post post3 = createTestPost("header3","text3",new Date(),"photo3",(long) 0,admin, Collections.singleton(createTestTheme("testTheme3")));

        List<Post> posts= new ArrayList<>();
        posts.add(post1);
        posts.add(post2);
        posts.add(post3);

        List<Post> result=postService.getAll();
        Assert.assertEquals(posts,result);
    }

    @Test
    public void getAll_ShouldReturnPostsAccordingUsersPreferencesInCountFavoriteThemesDescOrder() throws UserNotAuthorizedException, PostsNotFoundException {
        Date date=new Date();

        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        Theme themeFavorite1=createTestTheme("favoriteTheme1");
        Theme themeFavorite2=createTestTheme("favoriteTheme2");
        Theme themeFavorite3=createTestTheme("favoriteTheme3");
        Theme themeHated=createTestTheme("hatedTheme");
        createTestUserPreferences(user,themeFavorite1,true);
        createTestUserPreferences(user,themeHated,false);
        createTestUserPreferences(user,themeFavorite2,true);
        createTestUserPreferences(user,themeFavorite3,true);

        Set<Theme> favoriteThemesSet3=new HashSet<>();
        favoriteThemesSet3.add(themeFavorite2);
        favoriteThemesSet3.add(themeFavorite1);
        favoriteThemesSet3.add(themeFavorite3);

        Set<Theme> favoriteThemesSet2=new HashSet<>();
        favoriteThemesSet2.add(themeFavorite2);
        favoriteThemesSet2.add(themeFavorite3);

        Post post1 = createTestPost("FavoriteThemeNews1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(themeFavorite1));
        Post post2 = createTestPost("HatedThemeNews1","text2",new Date(),"photo2",(long) 0,admin, Collections.singleton(themeHated));
        Post post3 = createTestPost("FavoriteThemeNews2","text3",new Date(),"photo3",(long) 0,admin,favoriteThemesSet3);
        Post post4 = createTestPost("FavoriteThemeNews3","text4",new Date(),"photo3",(long) 0,admin,favoriteThemesSet2);

        List<Post> posts= new ArrayList<>();
        posts.add(post3);
        posts.add(post4);
        posts.add(post1);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));

        List<Post> result=postService.getAll();

        System.out.println(result.size());

        Assert.assertEquals(posts,result);


    }


    @Test
    public void getAll_ShouldThrowPostNotFoundExceptionIfNoPosts(){
        User admin = createTestUser("testAdmin@gmail.com","adminF","adminL","12345678",true);
        Post post1 = createTestPost("FavoriteThemeNews1","text1",new Date(),"photo1",(long) 0,admin, Collections.singleton(createTestTheme("testTheme")));

        Date date = new Date(); // Дата создания поста раньше, чем та, с которой мы начинаем поиск, поэтому он не должен найти посты


        try {
            List<Post> result=postService.getAll();
        } catch (PostsNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        } catch (UserNotAuthorizedException e) {
            return;
        }
    }


    @Test
    @Transactional
    public void getPostById_ShouldReturnPostById() throws PostNotFoundException {
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin,Collections.singleton(createTestTheme("testTheme1")));
        String id= String.valueOf(post.getId());
        Post result=postService.getPostById(id);
        Assert.assertEquals(post,result);

    }

    @Test
    public void getPostById_ShouldThrowPostNotFoundExceptionIfNoPostWithId(){
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin,Collections.singleton(createTestTheme("testTheme1")));
        String id="314134";
        try{
            Post result=postService.getPostById(id);
        }
        catch (PostNotFoundException e){
            System.out.println(e.getMessage());
            Assert.assertNotEquals("",e.getMessage());
        }
    }

    @Test
    public void createPost_ShouldCreatePost() throws CreatePostException {
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);

        SecurityContextHolder.getContext().setAuthentication(doAuth(admin.getEmail(),"12345678"));
        Theme theme1=createTestTheme("theme1");
        Theme theme2=createTestTheme("theme2");

        Set<String> themes=new HashSet<>();
        themes.add(theme1.getName());
        themes.add(theme2.getName());
        CreatePostRequest createPostRequest = new CreatePostRequest("header","text","photo", themes);

        Date date_before=new Date();
        postService.createPost(createPostRequest);
        Date date_after=new Date();

        Post post=postRepository.findByDateBetween(date_before,date_after);
        // Проверяем шо появился пост в промежуток, когда мы вызывали функцию, не знаю как по другому проверить создание поста :\
        Assert.assertEquals(createPostRequest.getHeader(),post.getHeader());
        Assert.assertEquals(createPostRequest.getText(),post.getText());
        Assert.assertEquals(createPostRequest.getPhoto(),post.getPhoto());
        Set<String> themes2=new HashSet<>();
        for(Theme theme: post.getThemes()){
            themes2.add(theme.getName());
        }
        Assert.assertEquals(themes,themes2);
    }
    @Test
    public void createPost_ShouldThrowExceptionIfAdminNotAuth(){
        // Не уверен в целесообразности данного теста, т.к. над контроллером стоит аннотация preAuthorize, что подразумевает, что
        // Пользователь не являющийся админом не сможет вызвать этот метод
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Theme theme1=createTestTheme("theme1");
        Theme theme2=createTestTheme("theme2");
        Set<String> themes=new HashSet<>();
        themes.add(theme1.getName());
        themes.add(theme2.getName());
        CreatePostRequest createPostRequest = new CreatePostRequest("header","text","photo", themes);
        try{
            postService.createPost(createPostRequest);
        } catch (CreatePostException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("",e.getMessage());
        }
    }

    @Test
    public void editPost_ShouldEditPostCorrectly() throws UserNotAuthorizedException, PostsNotFoundException {
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin,Collections.singleton(createTestTheme("testTheme1")));
        Set<String> themesStr=new HashSet<>();
        Set<Theme> themes=new HashSet<>();
        Theme theme1=createTestTheme("newTheme1");
        Theme theme2=createTestTheme("newTheme2");
        themesStr.add(theme1.getName());
        themesStr.add(theme2.getName());
        themes.add(theme1);
        themes.add(theme2);


        EditPostRequest editPostRequest = new EditPostRequest();
        editPostRequest.setHeader("new header");
        editPostRequest.setText("new text");
        editPostRequest.setPhoto("new Photo");
        editPostRequest.setThemes(themesStr);

        post.setHeader(editPostRequest.getHeader());
        post.setText(editPostRequest.getText());
        post.setPhoto(editPostRequest.getPhoto());
        post.setThemes(themes);

        postService.editPost(Long.toString(post.getId()),editPostRequest);

        Post result=postRepository.getPostById(post.getId()).get();


        Assert.assertEquals(post,result);


    }



    @Test
    public void editPost_ShouldThrowPostNotFoundException(){
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin,Collections.singleton(createTestTheme("testTheme1")));
        Set<String> themesStr=new HashSet<>();
        Theme theme1=createTestTheme("newTheme1");
        Theme theme2=createTestTheme("newTheme2");
        themesStr.add(theme1.getName());
        themesStr.add(theme2.getName());

        EditPostRequest editPostRequest = new EditPostRequest();
        editPostRequest.setHeader("new header");
        editPostRequest.setText("new text");
        editPostRequest.setPhoto("new Photo");
        editPostRequest.setThemes(themesStr);

        try {
            postService.editPost(Long.toString(51435),editPostRequest);
        } catch (PostsNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("",e.getMessage());
        } catch (UserNotAuthorizedException e) {
            throw new RuntimeException(e);
        }

    }




    @Test
    public void deletePost_ShouldDeletePostWithId() throws PostNotFoundException {
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin,Collections.singleton(createTestTheme("testTheme1")));

        Assert.assertTrue(postRepository.existsById(post.getId()));
        postService.deletePost(Long.toString(post.getId()));
        Assert.assertFalse(postRepository.existsById(post.getId()));
    }

    @Test
    public void deletePost_ShouldThrowPostNotFoundExceptionIfNoPostWithId(){
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin,Collections.singleton(createTestTheme("testTheme1")));

        try {
            postService.deletePost(Long.toString(541541));
        } catch (PostNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        }
    }

    @Test
    public void setLike_ShouldAddLikeToPostIfUserDidntLikePostBefore() throws PostNotFoundException, UserNotAuthorizedException {
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Set<Theme> themes=new HashSet<>();
        themes.add(createTestTheme("testTheme"));
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 0,admin,themes);


        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));

        Assert.assertTrue(user.getPosts().isEmpty());
        Assert.assertEquals(0,post.getLikes().longValue());

        postService.setLike(String.valueOf(post.getId()));

        Post result= postRepository.getPostById(post.getId()).get();
        User resultUser=userRepository.findByEmail(user.getEmail()).get();

        Assert.assertEquals(post.getLikes().longValue(),result.getLikes().longValue());
        Assert.assertTrue(resultUser.getPosts().contains(result));

    }

    @Test
    public void setLike_ShouldRemoveLikeIfUserLikedThisPostBefore() throws UserNotAuthorizedException, PostNotFoundException {
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);

        Set<Theme> themes=new HashSet<>();
        themes.add(createTestTheme("testTheme"));
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 1,admin,themes);

        Set<Post> posts=new HashSet<>();
        posts.add(post);
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        user.setPosts(posts);
        userRepository.saveAndFlush(user);

        Assert.assertTrue(user.getPosts().contains(post));
        Assert.assertEquals(1,post.getLikes().longValue());

        postService.setLike(String.valueOf(post.getId()));

        User resultUser= userRepository.findByEmail(user.getEmail()).get();
        Post postResult= postRepository.getPostById(post.getId()).get();

        Assert.assertEquals(post.getLikes().longValue(),postResult.getLikes().longValue());
        Assert.assertFalse(resultUser.getPosts().contains(post));



    }


    @Test
    public void setLike_ShouldThrowExceptionIfPostNotFound(){
        User admin = createTestUser("admin@gmail.com","adminF","adminL","12345678",true);
        Post post = createTestPost("header1","text1",new Date(),"photo1",(long) 1,admin,Collections.singleton(createTestTheme("testTheme1")));
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));

        try {
            postService.setLike(String.valueOf(154142531));
        } catch (PostNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        } catch (UserNotAuthorizedException e) {
            throw new RuntimeException(e);
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

    private void createTestUserPreferences(User user,Theme theme, boolean type){
        UserPreferencesId userPreferencesId=new UserPreferencesId(user.getId(),theme.getId());
        UserPreferences userPreferences=new UserPreferences(userPreferencesId,user,theme,type);
        userPreferencesRepository.saveAndFlush(userPreferences);
    }

    private Theme createTestTheme(String name){
        Theme theme = new Theme(name);
        return themeRepository.saveAndFlush(theme);
    }

//    @BeforeAll
//    public static void createTestRole(){
//        List<Role> roles= new ArrayList<>();
//        roles.add(new Role(ERole.ROLE_ADMIN));
//        roles.add(new Role(ERole.ROLE_USER));
//        roleRepository.saveAllAndFlush(roles);
//    }


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

}