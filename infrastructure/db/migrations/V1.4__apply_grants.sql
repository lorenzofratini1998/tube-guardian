-- GRANT on schemas
GRANT USAGE ON SCHEMA video_domain TO ms_orchestrator ;
GRANT USAGE ON SCHEMA analysis_domain TO ms_policy_engine;

-- Video Orchestrator
GRANT SELECT, INSERT, UPDATE, DELETE ON video_domain.videos TO ms_orchestrator ;
GRANT SELECT, INSERT, UPDATE, DELETE ON video_domain.analysis_jobs TO ms_orchestrator ;
GRANT SELECT, INSERT, UPDATE, DELETE ON video_domain.brand_profiles TO ms_orchestrator ;
GRANT SELECT ON analysis_domain.analysis_results TO ms_orchestrator ;

-- Policy Engine
GRANT SELECT, INSERT, UPDATE, DELETE ON analysis_domain.analysis_results TO ms_policy_engine;
GRANT SELECT, INSERT, UPDATE, DELETE ON analysis_domain.policy_versions TO ms_policy_engine;
GRANT SELECT ON video_domain.video_contents TO ms_policy_engine;