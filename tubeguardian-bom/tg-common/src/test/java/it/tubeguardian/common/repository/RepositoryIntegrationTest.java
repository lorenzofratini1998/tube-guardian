package it.tubeguardian.common.repository;


import it.tubeguardian.common.domain.AnalysisJob;
import it.tubeguardian.common.domain.AnalysisResult;
import it.tubeguardian.common.domain.BrandProfile;
import it.tubeguardian.common.domain.Video;
import it.tubeguardian.common.domain.risk.RiskLevel;
import it.tubeguardian.common.domain.status.JobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryIntegrationTest extends AbstractRepositoryTest {

    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private AnalysisJobRepository jobRepository;
    @Autowired
    private AnalysisResultRepository resultRepository;
    @Autowired
    private BrandProfileRepository brandProfileRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Mapping: Should correctly serialize/deserialize JSONB fields (RiskProfile)")
    void testJsonMapping() {
        Video video = videoRepository.save(Video.create("json_test", "Json Test", "Ch1"));

        var profile = new AnalysisResult.RiskProfile(
                RiskLevel.HIGH,
                10,
                "Dangerous",
                Map.of("violence", new AnalysisResult.CategoryDetail(RiskLevel.HIGH, "Fight"))
        );

        resultRepository.save(AnalysisResult.create(video, profile));

        var fetched = resultRepository.findByVideoId(video.getId()).orElseThrow();
        assertThat(fetched.getFullResponse().categories())
                .containsKey("violence")
                .satisfies(map -> assertThat(map.get("violence").riskLevel()).isEqualTo(RiskLevel.HIGH));
    }

    @Test
    @DisplayName("Custom Query: findTopByVideoIdOrderByCreatedAtDesc works correctly")
    void testCustomQuery() {
        Video video = videoRepository.save(Video.create("job_vid", "Job Test", "Ch1"));

        AnalysisJob jobOld = AnalysisJob.create(video);
        jobRepository.save(jobOld);

        AnalysisJob jobNew = AnalysisJob.create(video);
        jobNew.setStatus(JobStatus.PROCESSING);
        jobRepository.save(jobNew);

        Optional<AnalysisJob> result = jobRepository.findTopByVideoIdOrderByCreatedAtDesc(video.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(JobStatus.PROCESSING);
        assertThat(result.get().getId()).isNotEqualTo(jobOld.getId());
    }

    @Test
    @DisplayName("Constraints: Should violate unique constraint on YoutubeID")
    void testUniqueConstraint() {
        videoRepository.save(Video.create("duplicate", "V1", "C1"));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            videoRepository.saveAndFlush(Video.create("duplicate", "V2", "C2"));
        });
    }

    @Test
    @DisplayName("BrandProfile: Should save and retrieve JSONB tolerances map correctly")
    void testBrandProfileJsonMapping() {
        Map<String, String> tolerances = Map.of(
                "violence", "LOW",
                "adult_content", "NONE",
                "hate_speech", "MEDIUM"
        );

        String profileId = "disney_kids_v1";
        BrandProfile profile = BrandProfile.create(profileId, "Disney Kids", tolerances);

        brandProfileRepository.save(profile);

        entityManager.flush();
        entityManager.clear();

        Optional<BrandProfile> fetched = brandProfileRepository.findById(profileId);

        assertThat(fetched).isPresent();

        assertThat(fetched.get().getDisplayName()).isEqualTo("Disney Kids");

        Map<String, String> fetchedTolerances = fetched.get().getTolerances();

        assertThat(fetchedTolerances)
                .hasSize(3)
                .containsEntry("violence", "LOW")
                .containsEntry("adult_content", "NONE");
    }
}
