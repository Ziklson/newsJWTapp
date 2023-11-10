package nicetu.kuvarin.news.newsJwtApp.repository;

import nicetu.kuvarin.news.newsJwtApp.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ThemeRepository extends JpaRepository<Theme,Long> {

    public Set<Theme> findThemesByNameIn(Set<String> names);

    public Optional<Theme> findByName(String name);

    public Set<Theme> findAllByIdIn(Set<Long> ids);

}
