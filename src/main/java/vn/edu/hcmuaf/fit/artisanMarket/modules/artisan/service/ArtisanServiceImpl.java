package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanCardResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanDetailResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanSkill;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository.ArtisanRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtisanServiceImpl implements ArtisanService {

    private final ArtisanRepository artisanRepository;

    @Override
    public List<ArtisanCardResponse> getArtisansMarket(String skill, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, "rating");

        boolean hasCustomSort = false;

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            switch (sortBy) {
                case "experience" -> {
                    sort = Sort.by(Sort.Direction.ASC, "startedCraftingDate");
                    hasCustomSort = true;
                }
                case "orders" -> {
                    sort = Sort.by(Sort.Direction.DESC, "totalOrders");
                    hasCustomSort = true;
                }
                case "rating" -> {
                    sort = Sort.by(Sort.Direction.DESC, "rating");
                    hasCustomSort = true;
                }
            }
        }

        List<Artisan> artisans;

        List<ArtisanStatus> allowedStatuses = List.of(ArtisanStatus.ACTIVE, ArtisanStatus.BUSY);

        if (skill == null || skill.equalsIgnoreCase("ALL") || skill.isEmpty()) {
            artisans = artisanRepository.findAll(sort);
        } else {
            try {
                ArtisanSkill artisanSkill = ArtisanSkill.valueOf(skill.toUpperCase());
                artisans = artisanRepository.findAll(sort).stream()
                        .filter(a -> a.getSkill() == artisanSkill)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                artisans = artisanRepository.findAll(sort);
            }
        }

        var stream = artisans.stream();

        if (!hasCustomSort) {

            stream = stream.sorted((a, b) -> {
                boolean f1 = a.getFeatured() != null && a.getFeatured();
                boolean f2 = b.getFeatured() != null && b.getFeatured();
                return Boolean.compare(f2, f1); // Nổi bật lên đầu
            });
        }

        return stream.map(ArtisanCardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ArtisanDetailResponse getArtisanDetail(Long id) {
        Artisan artisan = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nghệ nhân với ID: " + id));
        return ArtisanDetailResponse.fromEntity(artisan);
    }

    @Override
    @Transactional
    public void processNewOrder(Long id) {
        Artisan artisan = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nghệ nhân với ID: " + id));
        artisan.acceptNewOrder();
        artisanRepository.save(artisan);
    }

    @Override
    @Transactional
    public void processCompleteOrder(Long id) {
        Artisan artisan = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nghệ nhân với ID: " + id));
        artisan.completeOrder();
        artisanRepository.save(artisan);
    }
}