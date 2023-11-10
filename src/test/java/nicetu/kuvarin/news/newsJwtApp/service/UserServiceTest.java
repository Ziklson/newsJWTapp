package nicetu.kuvarin.news.newsJwtApp.service;

import nicetu.kuvarin.news.newsJwtApp.exception.ThemeNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserNotAuthorizedException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserPreferensesNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.WrongTypeOfPreferencesException;
import nicetu.kuvarin.news.newsJwtApp.mapper.PostMapper;
import nicetu.kuvarin.news.newsJwtApp.model.*;
import nicetu.kuvarin.news.newsJwtApp.payload.request.AddToPreferencesRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.RemoveFromPreferencesRequest;
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


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class UserServiceTest {
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

    @Autowired
    UserService userService;


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
    public void getAll_ShouldReturnAllUsersPreferences() throws UserPreferensesNotFoundException, UserNotAuthorizedException {
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));

        Theme theme1=createTestTheme("testFavoriteTheme");
        Theme theme2=createTestTheme("testHatedTheme");


        UserPreferences userPreferences1= createTestUserPreferences(user,theme1,true);
        UserPreferences userPreferences2= createTestUserPreferences(user,theme2,false);

        List<UserPreferences> userPreferencesList=new ArrayList<>();
        userPreferencesList.add(userPreferences1);
        userPreferencesList.add(userPreferences2);

        List<UserPreferences> result=userService.getAll();

        Assert.assertEquals(userPreferencesList,result);

    }

    @Test
    public void getAll_ShouldThrowUserPreferencesNotFoundExceptionIfUserPreferencesIsEmpty(){
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        try {
            List<UserPreferences> result=userService.getAll();
        } catch (UserPreferensesNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        } catch (UserNotAuthorizedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getAll_ShouldThrowUserNotAuthException(){
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        try {
            List<UserPreferences> result=userService.getAll();
        } catch (UserPreferensesNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UserNotAuthorizedException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        }
    }

    @Test
    public void addToPreferences_ShouldAddPreferencesToUserPreferences() throws UserNotAuthorizedException, ThemeNotFoundException, WrongTypeOfPreferencesException {
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        Theme theme=createTestTheme("favoriteTheme");
        AddToPreferencesRequest addToPreferencesRequest=new AddToPreferencesRequest(theme.getName(),"нравится");

        userService.addToPreferences(addToPreferencesRequest);

        UserPreferences userPreferences=userPreferencesRepository.findAllByUser(user).get(0);
        Assert.assertEquals(theme.getName(),userPreferences.getTheme().getName());
        Assert.assertTrue(userPreferences.isType());
    }

    @Test
    public void addToPreferences_ShouldThrowUserNotAuthException(){
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        Theme theme=createTestTheme("favoriteTheme");
        AddToPreferencesRequest addToPreferencesRequest=new AddToPreferencesRequest(theme.getName(),"нравится");

        try {
            userService.addToPreferences(addToPreferencesRequest);
        } catch (UserNotAuthorizedException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        } catch (ThemeNotFoundException | WrongTypeOfPreferencesException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void addToPreferences_ShouldThrowThemeNotFoundException(){
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        AddToPreferencesRequest addToPreferencesRequest=new AddToPreferencesRequest("notExistedTheme","нравится");

        try {
            userService.addToPreferences(addToPreferencesRequest);
        } catch (UserNotAuthorizedException | WrongTypeOfPreferencesException e) {
            throw new RuntimeException(e);
        } catch (ThemeNotFoundException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        }
    }

    @Test
    public void addToPreferences_ShouldThrowWrongTypeOfPreferencsesException(){
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        Theme theme=createTestTheme("favoriteTheme");

        AddToPreferencesRequest addToPreferencesRequest=new AddToPreferencesRequest(theme.getName(),"wrongType");
        try {
            userService.addToPreferences(addToPreferencesRequest);
        } catch (UserNotAuthorizedException | ThemeNotFoundException e) {
            throw new RuntimeException(e);
        } catch (WrongTypeOfPreferencesException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        }
    }

    @Test
    public void removeFromPreferences_ShouldRemoveFromUserPreferences() throws UserNotAuthorizedException, ThemeNotFoundException {
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        Theme theme=createTestTheme("favoriteTheme");
        UserPreferences userPreferences=createTestUserPreferences(user,theme,true);
        Assert.assertTrue(userPreferencesRepository.existsById(userPreferences.getUserPreferencesId()));
        RemoveFromPreferencesRequest removeFromPreferencesRequest=new RemoveFromPreferencesRequest("favoriteTheme");
        userService.removeFromPreferences(removeFromPreferencesRequest);
        Assert.assertFalse(userPreferencesRepository.existsById(userPreferences.getUserPreferencesId()));
    }


    @Test
    public void removeFromPreferences_ShouldThrowUserNotAuthException(){
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        Theme theme=createTestTheme("favoriteTheme");
        UserPreferences userPreferences=createTestUserPreferences(user,theme,true);
        RemoveFromPreferencesRequest removeFromPreferencesRequest=new RemoveFromPreferencesRequest("favoriteTheme");

        try {
            userService.removeFromPreferences(removeFromPreferencesRequest);
        } catch (UserNotAuthorizedException e) {
            System.out.println(e.getMessage());
            Assert.assertNotEquals("", e.getMessage());
        } catch (ThemeNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void removeFromPreferences_ShouldThrowThemeNotFoundException(){
        User user=createTestUser("testUser@gmail.com","userF","userL","12345678",false);
        SecurityContextHolder.getContext().setAuthentication(doAuth(user.getEmail(), "12345678"));
        Theme theme=createTestTheme("favoriteTheme");
        UserPreferences userPreferences=createTestUserPreferences(user,theme,true);
        RemoveFromPreferencesRequest removeFromPreferencesRequest=new RemoveFromPreferencesRequest("unknownThemeName");

        try {
            userService.removeFromPreferences(removeFromPreferencesRequest);
        } catch (UserNotAuthorizedException e) {
            throw new RuntimeException(e);
        } catch (ThemeNotFoundException e) {
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

    private UserPreferences createTestUserPreferences(User user,Theme theme, boolean type){
        UserPreferencesId userPreferencesId=new UserPreferencesId(user.getId(),theme.getId());
        UserPreferences userPreferences=new UserPreferences(userPreferencesId,user,theme,type);
        return userPreferencesRepository.saveAndFlush(userPreferences);
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


}