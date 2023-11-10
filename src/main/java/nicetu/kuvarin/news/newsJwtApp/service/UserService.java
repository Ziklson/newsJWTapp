package nicetu.kuvarin.news.newsJwtApp.service;


import nicetu.kuvarin.news.newsJwtApp.exception.ThemeNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserNotAuthorizedException;
import nicetu.kuvarin.news.newsJwtApp.exception.UserPreferensesNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.exception.WrongTypeOfPreferencesException;
import nicetu.kuvarin.news.newsJwtApp.mapper.UserPreferencesMapper;
import nicetu.kuvarin.news.newsJwtApp.model.Theme;
import nicetu.kuvarin.news.newsJwtApp.model.User;
import nicetu.kuvarin.news.newsJwtApp.model.UserPreferences;
import nicetu.kuvarin.news.newsJwtApp.model.UserPreferencesId;
import nicetu.kuvarin.news.newsJwtApp.payload.request.AddToPreferencesRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.RemoveFromPreferencesRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.response.UserPreferencesResponse;
import nicetu.kuvarin.news.newsJwtApp.repository.ThemeRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.UserPreferencesRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    UserPreferencesRepository userPreferencesRepository;

    @Autowired
    UserPreferencesMapper userPreferencesMapper;


    public List<UserPreferences> getAll() throws UserPreferensesNotFoundException, UserNotAuthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Optional<User> userOpt=userRepository.findByEmail(userDetails.getUsername());

            if(userOpt.isPresent()){
                User user=userOpt.get();
                List<UserPreferences> userPreferencesList=userPreferencesRepository.findAllByUser(user);
                if(userPreferencesList.isEmpty()){
                    throw new UserPreferensesNotFoundException("Предпочтения пользователя не найдены");
                }
                else
                    return userPreferencesList;
            }
            else
                throw new UserNotAuthorizedException("Пользователь не авторизован");
        }
        else{
            throw new UserNotAuthorizedException("Пользователь не авторизован");
        }



    }

    @Transactional
    public void addToPreferences(AddToPreferencesRequest addToPreferencesRequest) throws UserNotAuthorizedException, ThemeNotFoundException, WrongTypeOfPreferencesException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            boolean type;
            if(addToPreferencesRequest.getType().equalsIgnoreCase("нравится")){
                type=true;
            }
            else{
                if(addToPreferencesRequest.getType().equalsIgnoreCase("не нравится"))
                    type=false;
                else
                    throw new WrongTypeOfPreferencesException("Не существующий тип предпочтений");
            }
            Optional<User> userOpt=userRepository.findByEmail(userDetails.getUsername());
            Optional<Theme> themeOpt=themeRepository.findByName(addToPreferencesRequest.getName());
            if(themeOpt.isPresent()){
                Theme theme=themeOpt.get();
                if(userOpt.isPresent()){
                    User user=userOpt.get();
                    UserPreferences userPreferences;
                    UserPreferencesId userPreferencesId=new UserPreferencesId(user.getId(),theme.getId());
                    Optional<UserPreferences> userPreferencesOpt=userPreferencesRepository.findById(userPreferencesId);
                    if(userPreferencesOpt.isPresent()){
                        userPreferences=userPreferencesOpt.get();
                        userPreferences.setType(type);
                    }
                    else{
                        userPreferences=new UserPreferences(userPreferencesId,user,theme,type);
                    }
                    userPreferencesRepository.save(userPreferences);
                }
                else
                    throw new UserNotAuthorizedException("Пользователь не авторизован");
            }
            else
                throw new ThemeNotFoundException("Тема не найдена");
        }
        else{
            throw new UserNotAuthorizedException("Пользователь не авторизован");
        }






    }



    public void removeFromPreferences(RemoveFromPreferencesRequest removeFromPreferencesRequest) throws UserNotAuthorizedException, ThemeNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            Optional<User> userOpt=userRepository.findByEmail(userDetails.getUsername());
            Optional<Theme> themeOpt=themeRepository.findByName(removeFromPreferencesRequest.getName());

            if(userOpt.isPresent()){
                if(themeOpt.isPresent()){
                    User user=userOpt.get();
                    Theme theme=themeOpt.get();
                    UserPreferencesId userPreferencesId=new UserPreferencesId(user.getId(),theme.getId());
                    Optional<UserPreferences> userPreferencesOpt=userPreferencesRepository.findById(userPreferencesId);
                    userPreferencesOpt.ifPresent(userPreferences -> userPreferencesRepository.delete(userPreferences));
                }
                else
                    throw new ThemeNotFoundException("Тема не найдена");
            }
            else
                throw new UserNotAuthorizedException("Пользователь не авторизован");
        }
        else
            throw new UserNotAuthorizedException("Пользователь не авторизован");
    }
    public List<UserPreferencesResponse> getUserPreferencesResponseList(List<UserPreferences> userPreferences){
        return userPreferencesMapper.toUserPreferencesResponseList(userPreferences);
    }




}
