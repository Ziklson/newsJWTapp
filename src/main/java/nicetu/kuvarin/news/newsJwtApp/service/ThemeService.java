package nicetu.kuvarin.news.newsJwtApp.service;


import nicetu.kuvarin.news.newsJwtApp.exception.ThemeNotFoundException;
import nicetu.kuvarin.news.newsJwtApp.mapper.ThemeMapper;
import nicetu.kuvarin.news.newsJwtApp.model.Theme;
import nicetu.kuvarin.news.newsJwtApp.repository.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ThemeService {
    
    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ThemeMapper themeMapper;

//    public Set<Theme> getThemes(Set<String> themes) throws ThemeNotFoundException {
//
//        Set<Theme> result = themeRepository.findThemesByNameIn(themes);
//        if(result.isEmpty())
//            throw new ThemeNotFoundException("Темы не найдены");
//        return result;
//    }


    public List<String> getThemesNames(Set<Theme> themes){
        return themeMapper.toStringThemesList(themes);
    }


}
