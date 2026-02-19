package io.tubeguardian.orchestrator.api;

import io.tubeguardian.orchestrator.api.dto.BrandProfileDto;
import io.tubeguardian.orchestrator.repository.BrandProfileRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

  private final BrandProfileRepository brandProfileRepository;

  public BrandController(BrandProfileRepository brandProfileRepository) {
    this.brandProfileRepository = brandProfileRepository;
  }

  @GetMapping
  public ResponseEntity<List<BrandProfileDto>> getAllBrands() {
    return ResponseEntity.ok(
        brandProfileRepository.findAll().stream().map(BrandProfileDto::fromEntity).toList());
  }
}
