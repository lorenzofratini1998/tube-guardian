SET search_path TO video_domain;

INSERT INTO brand_profiles (profile_id, display_name, logo_url, tolerances, created_at, updated_at)
VALUES ('DISNEY',
        'Disney',
        'https://upload.wikimedia.org/wikipedia/commons/3/3e/Disney%2B_logo.svg',
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

INSERT INTO brand_profiles (profile_id, display_name, logo_url, tolerances, created_at, updated_at)
VALUES ('REDBULL',
        'Red Bull',
        'https://upload.wikimedia.org/wikipedia/en/f/f5/RedBullEnergyDrink.svg',
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

INSERT INTO brand_profiles (profile_id, display_name, logo_url, tolerances, created_at, updated_at)
VALUES ('HEINEKEN',
        'Heineken',
        'https://upload.wikimedia.org/wikipedia/commons/0/0d/Heineken_Logo.svg',
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

INSERT INTO brand_profiles (profile_id, display_name, logo_url, tolerances, created_at, updated_at)
VALUES ('NYT',
        'The New York Times',
        'https://upload.wikimedia.org/wikipedia/commons/7/77/The_New_York_Times_logo.png',
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

INSERT INTO brand_profiles (profile_id, display_name, logo_url, tolerances, created_at, updated_at)
VALUES ('DOVE',
        'Dove',
        'https://upload.wikimedia.org/wikipedia/commons/b/b4/Dove_%282004%29.svg',
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