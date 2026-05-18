package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanSkill;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtisanRepository extends JpaRepository<Artisan, Long> {


    Optional<Artisan> findByUserId(Long userId);
    List<Artisan> findByStatus(ArtisanStatus status, Sort sort);
    List<Artisan> findBySkillAndStatus(ArtisanSkill skill, ArtisanStatus status, Sort sort);
}