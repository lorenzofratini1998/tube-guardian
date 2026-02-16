SET search_path TO analysis_domain;
-------------------------------------------------------------
CREATE TABLE analysis_results
(
    id             UUID   NOT NULL,
    version        BIGINT NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    video_id       UUID   NOT NULL,
    model_used     VARCHAR(50),
    prompt_version VARCHAR(20),
    full_response  JSONB  NOT NULL,
    analyzed_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_analysis_results PRIMARY KEY (id)
);

CREATE INDEX idx_analysis_json ON analysis_results USING GIN (full_response);

ALTER TABLE analysis_results
    ADD CONSTRAINT FK_ANALYSIS_RESULTS_ON_VIDEO FOREIGN KEY (video_id) REFERENCES video_domain.videos (id);
-------------------------------------------------------------
CREATE TABLE policy_versions
(
    policy_name  VARCHAR(255) NOT NULL,
    content_hash VARCHAR(255) NOT NULL,
    last_updated TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_policy_versions PRIMARY KEY (policy_name)
);