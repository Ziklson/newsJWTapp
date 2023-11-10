package nicetu.kuvarin.news.newsJwtApp.mapper;


import nicetu.kuvarin.news.newsJwtApp.model.Theme;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ThemeMapper {

    public List<String> toStringThemesList(Set<Theme> themes){
        List<String> result=new ArrayList<>();
        for (Theme theme : themes) {
            result.add(theme.getName());
        }
        return result;
    }
}
