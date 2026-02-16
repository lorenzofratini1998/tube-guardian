CREATE TABLE policy_versions
(
    policy_name  VARCHAR(255) NOT NULL,
    content_hash VARCHAR(255) NOT NULL,
    last_updated TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_policy_versions PRIMARY KEY (policy_name)
);