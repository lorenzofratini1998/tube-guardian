SET search_path TO video_domain;
-------------------------------------------------------------
CREATE TABLE videos
(
    id               UUID         NOT NULL,
    version          BIGINT       NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    youtube_id       VARCHAR(20)  NOT NULL,
    title            VARCHAR(255) NOT NULL,
    channel          VARCHAR(255) NOT NULL,
    webpage_url      VARCHAR(255),
    upload_date      DATE,
    duration_seconds INTEGER,
    thumbnail_url    VARCHAR(255),
    CONSTRAINT pk_videos PRIMARY KEY (id)
);

ALTER TABLE videos
    ADD CONSTRAINT uk_videos_youtube_id UNIQUE (youtube_id);
-------------------------------------------------------------
CREATE TABLE video_contents
(
    video_id        UUID NOT NULL,
    description     TEXT,
    transcript_text TEXT,
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_video_contents PRIMARY KEY (video_id)
);

ALTER TABLE video_contents
    ADD CONSTRAINT FK_VIDEO_CONTENTS_ON_VIDEO FOREIGN KEY (video_id) REFERENCES videos (id);
-------------------------------------------------------------
CREATE TABLE analysis_jobs
(
    id            UUID        NOT NULL,
    version       BIGINT      NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    video_id      UUID        NOT NULL,
    status        VARCHAR(20) NOT NULL,
    error_message TEXT,
    CONSTRAINT pk_analysis_jobs PRIMARY KEY (id)
);

CREATE INDEX idx_jobs_status ON analysis_jobs (status);

ALTER TABLE analysis_jobs
    ADD CONSTRAINT FK_ANALYSIS_JOBS_ON_VIDEO FOREIGN KEY (video_id) REFERENCES videos (id);
-------------------------------------------------------------
CREATE TABLE brand_profiles
(
    profile_id   VARCHAR(50)  NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    tolerances   JSONB        NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_brand_profiles PRIMARY KEY (profile_id)
);