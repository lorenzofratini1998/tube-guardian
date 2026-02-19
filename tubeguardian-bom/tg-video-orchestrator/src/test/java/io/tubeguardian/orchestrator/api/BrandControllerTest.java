package io.tubeguardian.orchestrator.api;

import io.tubeguardian.common.domain.BrandProfile;
import io.tubeguardian.orchestrator.repository.BrandProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandController.class)
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandProfileRepository brandProfileRepository;

    @Test
    void getAllBrands_ShouldReturn200AndListOfBrands() throws Exception {
        BrandProfile disney = BrandProfile.create(
                "DISNEY",
                "Disney",
                "https://logo.url/disney.svg",
                Map.of("GARM-ADULT", "LOW")
        );
        BrandProfile redbull = BrandProfile.create(
                "REDBULL",
                "Red Bull",
                "https://logo.url/redbull.svg",
                Map.of("GARM-ADULT", "LOW")
        );

        when(brandProfileRepository.findAll()).thenReturn(List.of(disney, redbull));

        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("DISNEY"))
                .andExpect(jsonPath("$[0].displayName").value("Disney"))
                .andExpect(jsonPath("$[0].logoUrl").value("https://logo.url/disney.svg"))
                .andExpect(jsonPath("$[1].id").value("REDBULL"));
    }

    @Test
    void getAllBrands_WhenNoBrands_ShouldReturn200AndEmptyList() throws Exception {
        when(brandProfileRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
