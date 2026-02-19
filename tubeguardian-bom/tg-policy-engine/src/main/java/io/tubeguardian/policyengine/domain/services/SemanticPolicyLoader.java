package io.tubeguardian.policyengine.domain.services;

import io.tubeguardian.policyengine.domain.model.DocumentFactory;
import io.tubeguardian.policyengine.domain.model.GarmPolicy;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class SemanticPolicyLoader implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(SemanticPolicyLoader.class);

  private final VectorStore vectorStore;
  private final PolicyVersionService policyVersionService;
  private final DocumentFactory documentFactory;
  private final Resource policyResource;
  private final ObjectMapper objectMapper;

  public SemanticPolicyLoader(
      VectorStore vectorStore,
      PolicyVersionService policyVersionService,
      DocumentFactory documentFactory,
      @Value("${app.garm.policy-file:classpath:policies/garm_policies.json}") Resource policyResource,
      ObjectMapper objectMapper) {
    this.vectorStore = vectorStore;
    this.policyVersionService = policyVersionService;
    this.documentFactory = documentFactory;
    this.policyResource = policyResource;
    this.objectMapper = objectMapper;
  }

  @Override
  public void run(String... args) throws Exception {
    try {
      byte[] fileContent = policyResource.getContentAsByteArray();
      String currentHash = DigestUtils.md5DigestAsHex(fileContent);

      if (policyVersionService.isUpToDate(currentHash)) {
        log.info("GARM Policies are up-to-date (Hash: {})", currentHash);
        return;
      }

      log.info("Policy updated detected. Starting vectorization...");

      List<GarmPolicy> policies = objectMapper.readValue(fileContent, new TypeReference<>() {});
      List<Document> documents = policies.stream().map(documentFactory::create).toList();

      vectorStore.add(documents);
      policyVersionService.updateVersion(currentHash);

      log.info("Successfully loaded {} policies into Qdrant.", documents.size());

    } catch (Exception e) {
      log.error("Failed to initialize GARM Policies", e);
      throw new RuntimeException(e);
    }
  }
}
