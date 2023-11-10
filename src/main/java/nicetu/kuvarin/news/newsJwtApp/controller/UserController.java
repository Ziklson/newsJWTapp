package nicetu.kuvarin.news.newsJwtApp.controller;


import nicetu.kuvarin.news.newsJwtApp.exception.ThemeNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserNotAuthorizedException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserPreferensesNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.WrongTypeOfPreferencesException;
import nicetu.kuvarin.news.newsJwtApp.payload.request.AddToPreferencesRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.RemoveFromPreferencesRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.response.MessageResponse;
import nicetu.kuvarin.news.newsJwtApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/favorites")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getFavoritesThemes(){
        try{
            return ResponseEntity.ok(userService.getUserPreferencesResponseList(userService.getAll()));
        } catch (UserPreferensesNotFoundException | UserNotAuthorizedException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/favorites")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addToFavoritesThemes(@RequestBody AddToPreferencesRequest addToPreferencesRequest){
        try{
            userService.addToPreferences(addToPreferencesRequest);
            return ResponseEntity.ok(new MessageResponse("Предпочтения успешно добавлены"));
        } catch (UserNotAuthorizedException | ThemeNotFoundException | WrongTypeOfPreferencesException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }

    }

    @DeleteMapping("/favorites")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeFromFavoritesThemes(@RequestBody RemoveFromPreferencesRequest removeFromPreferencesRequest){
        try{
            userService.removeFromPreferences(removeFromPreferencesRequest);
            return ResponseEntity.ok(new MessageResponse("Тема успешно удалена из предпочтений"));
        } catch (UserNotAuthorizedException | ThemeNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }



}
