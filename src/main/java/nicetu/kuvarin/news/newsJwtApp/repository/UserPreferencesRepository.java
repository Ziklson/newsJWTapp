package nicetu.kuvarin.news.newsJwtApp.repository;


import nicetu.kuvarin.news.newsJwtApp.model.User;
import nicetu.kuvarin.news.newsJwtApp.model.UserPreferences;
import nicetu.kuvarin.news.newsJwtApp.model.UserPreferencesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UserPreferencesId> {
    public Optional<UserPreferences> findById(UserPreferencesId id);

    public List<UserPreferences> findAllByUser(User user);


}
