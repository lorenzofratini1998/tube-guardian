package io.tubeguardian.policyengine.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.tubeguardian.policyengine.AbstractIntegrationTest;
import io.tubeguardian.policyengine.domain.repository.PolicyVersionRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class SemanticPolicyLoaderIT extends AbstractIntegrationTest {

  @Autowired private SemanticPolicyLoader loader;

  @Autowired private PolicyVersionRepository versionRepository;

  @MockitoBean private VectorStore vectorStore;

  @BeforeEach
  void setup() {
    versionRepository.deleteAll();
    Mockito.clearInvocations(vectorStore);
  }

  @Test
  @DisplayName("Should load policies into VectorStore and update DB hash on first run")
  void run_FirstExecution_ShouldProcessPolicies() throws Exception {
    loader.run();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Document>> docCaptor = ArgumentCaptor.forClass(List.class);
    verify(vectorStore, times(1)).add(docCaptor.capture());

    List<Document> documents = docCaptor.getValue();
    assertThat(documents).hasSize(1);
    assertThat(documents.get(0).getMetadata()).containsEntry("policy_id", "TEST-POLICY-01");

    assertThat(versionRepository.count()).isEqualTo(1);
    assertThat(versionRepository.findAll().get(0).getContentHash()).isNotNull();
  }

  @Test
  @DisplayName("Should skip loading if hash in DB matches file hash")
  void run_SecondExecution_SameFile_ShouldSkip() throws Exception {
    loader.run();
    clearInvocations(vectorStore);
    loader.run();

    verify(vectorStore, never()).add(anyList());
    assertThat(versionRepository.count()).isEqualTo(1);
  }
}
