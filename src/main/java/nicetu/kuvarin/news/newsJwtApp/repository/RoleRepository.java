package nicetu.kuvarin.news.newsJwtApp.repository;

import nicetu.kuvarin.news.newsJwtApp.model.ERole;
import nicetu.kuvarin.news.newsJwtApp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(ERole name);
}
