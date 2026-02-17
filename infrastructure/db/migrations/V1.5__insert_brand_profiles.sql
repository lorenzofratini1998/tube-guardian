SET search_path TO video_domain;

INSERT INTO brand_profiles (profile_id, display_name, tolerances, created_at, updated_at)
VALUES ('DISNEY',
        'Disney',
        '{
          "GARM-ADULT": "LOW",
          "GARM-ARMS": "LOW",
          "GARM-CRIME": "LOW",
          "GARM-DEATH": "LOW",
          "GARM-PIRACY": "LOW",
          "GARM-HATE": "LOW",
          "GARM-OBSCENE": "LOW",
          "GARM-DRUGS": "LOW",
          "GARM-SPAM": "LOW",
          "GARM-TERROR": "LOW",
          "GARM-SOCIAL": "LOW"
        }'::jsonb,
        NOW(),
        NOW())
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO brand_profiles (profile_id, display_name, tolerances, created_at, updated_at)
VALUES ('REDBULL',
        'Red Bull',
        '{
          "GARM-ADULT": "LOW",
          "GARM-ARMS": "HIGH",
          "GARM-CRIME": "LOW",
          "GARM-DEATH": "MEDIUM",
          "GARM-PIRACY": "LOW",
          "GARM-HATE": "LOW",
          "GARM-OBSCENE": "HIGH",
          "GARM-DRUGS": "LOW",
          "GARM-SPAM": "LOW",
          "GARM-TERROR": "LOW",
          "GARM-SOCIAL": "MEDIUM"
        }'::jsonb,
        NOW(),
        NOW())
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO brand_profiles (profile_id, display_name, tolerances, created_at, updated_at)
VALUES ('HEINEKEN',
        'Heineken',
        '{
          "GARM-ADULT": "MEDIUM",
          "GARM-ARMS": "LOW",
          "GARM-CRIME": "LOW",
          "GARM-DEATH": "LOW",
          "GARM-PIRACY": "LOW",
          "GARM-HATE": "LOW",
          "GARM-OBSCENE": "MEDIUM",
          "GARM-DRUGS": "HIGH",
          "GARM-SPAM": "LOW",
          "GARM-TERROR": "LOW",
          "GARM-SOCIAL": "MEDIUM"
        }'::jsonb,
        NOW(),
        NOW())
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO brand_profiles (profile_id, display_name, tolerances, created_at, updated_at)
VALUES ('NYT',
        'The New York Times',
        '{
          "GARM-ADULT": "LOW",
          "GARM-ARMS": "MEDIUM",
          "GARM-CRIME": "MEDIUM",
          "GARM-DEATH": "MEDIUM",
          "GARM-PIRACY": "LOW",
          "GARM-HATE": "LOW",
          "GARM-OBSCENE": "LOW",
          "GARM-DRUGS": "LOW",
          "GARM-SPAM": "LOW",
          "GARM-TERROR": "MEDIUM",
          "GARM-SOCIAL": "HIGH"
        }'::jsonb,
        NOW(),
        NOW())
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO brand_profiles (profile_id, display_name, tolerances, created_at, updated_at)
VALUES ('DOVE',
        'Dove',
        '{
          "GARM-ADULT": "LOW",
          "GARM-ARMS": "LOW",
          "GARM-CRIME": "LOW",
          "GARM-DEATH": "LOW",
          "GARM-PIRACY": "LOW",
          "GARM-HATE": "LOW",
          "GARM-OBSCENE": "LOW",
          "GARM-DRUGS": "LOW",
          "GARM-SPAM": "LOW",
          "GARM-TERROR": "LOW",
          "GARM-SOCIAL": "MEDIUM"
        }'::jsonb,
        NOW(),
        NOW())
ON CONFLICT (profile_id) DO NOTHING;