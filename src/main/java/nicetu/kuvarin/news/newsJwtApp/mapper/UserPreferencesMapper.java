package nicetu.kuvarin.news.newsJwtApp.mapper;


import nicetu.kuvarin.news.newsJwtApp.model.UserPreferences;
import nicetu.kuvarin.news.newsJwtApp.payload.response.UserPreferencesResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserPreferencesMapper {

    public List<UserPreferencesResponse> toUserPreferencesResponseList(List<UserPreferences> userPreferences){
        List<UserPreferencesResponse> result=new ArrayList<>();
        String type= "";
        for(UserPreferences userPreference: userPreferences){
            if(userPreference.isType())
                type="Нравится";
            else
                type="Не нравится";
            result.add(new UserPreferencesResponse(userPreference.getTheme().getName(),type));
        }
        return result;
    }
}
