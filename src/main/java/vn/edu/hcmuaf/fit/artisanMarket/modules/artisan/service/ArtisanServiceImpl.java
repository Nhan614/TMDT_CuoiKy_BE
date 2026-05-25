package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanSkill;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository.ArtisanRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtisanServiceImpl implements ArtisanService {

    private final ArtisanRepository artisanRepository;

    @Override
    public Page<ArtisanCardResponse> getArtisansMarket(String skill, String sortBy, Pageable pageable) {

        Sort sort = Sort.by(Sort.Direction.DESC, "rating");

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            switch (sortBy) {
                case "experience" -> sort = Sort.by(Sort.Direction.ASC, "startedCraftingDate");
                case "orders" -> sort = Sort.by(Sort.Direction.DESC, "totalOrders");
                case "rating" -> sort = Sort.by(Sort.Direction.DESC, "rating");
            }
        } else {

            sort = Sort.by(Sort.Direction.DESC, "featured")
                    .and(Sort.by(Sort.Direction.DESC, "rating"));
        }


        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);


        ArtisanSkill artisanSkill = null;
        if (skill != null && !skill.equalsIgnoreCase("ALL") && !skill.isEmpty()) {
            try {
                artisanSkill = ArtisanSkill.valueOf(skill.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }

        List<ArtisanStatus> allowedStatuses = List.of(ArtisanStatus.ACTIVE, ArtisanStatus.BUSY);

        Page<Artisan> artisanPage;
        if (artisanSkill == null) {
            artisanPage = artisanRepository.findByStatusIn(allowedStatuses, pageableWithSort);
        } else {
            artisanPage = artisanRepository.findByStatusInAndSkill(allowedStatuses, artisanSkill, pageableWithSort);
        }

        return artisanPage.map(ArtisanCardResponse::fromEntity);
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

    @Override
    public ArtisanProfileResponse getArtisanProfile(Long id) {

        Artisan artisan = artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nghệ nhân với ID: " + id));


        List<ArtisanProductResponse> fakeProducts = List.of(
                new ArtisanProductResponse(101L, "Tác phẩm len nghệ thuật giới hạn", "https://images.unsplash.com/photo-1608248597481-496100c80836", 350000.0, 45),
                new ArtisanProductResponse(102L, "Thú bông Amigurumi cao cấp", "https://images.unsplash.com/photo-1559251606-c623743a6d76", 220000.0, 89),
                new ArtisanProductResponse(103L, "Khăn choàng dệt tay hoạ tiết cổ điển", "https://images.unsplash.com/photo-1584917865442-de89df76afd3", 540000.0, 21)
        );


        List<ArtisanReviewResponse> fakeReviews = List.of(
                new ArtisanReviewResponse(201L, "Nguyễn Văn Minh", "https://api.dicebear.com/7.x/avataaars/svg?seed=Minh", 5.0, "Đường kim mũi chỉ cực kỳ đều và đẹp, nghệ nhân đóng gói cẩn thận, phục vụ tận tình.", LocalDate.now().minusDays(3)),
                new ArtisanReviewResponse(202L, "Trần Thị Thuỳ", "https://api.dicebear.com/7.x/avataaars/svg?seed=Thuy", 4.5, "Sản phẩm phác thảo giống hệt thực tế. Tuy nhiên tiến độ hơi chậm một chút do nghệ nhân kín lịch, bù lại chất lượng rất đáng tiền!", LocalDate.now().minusWeeks(2))
        );


        return ArtisanProfileResponse.fromEntity(artisan, fakeProducts, fakeReviews);
    }
}