UPDATE workspace
SET document = jsonb_set(document, '{platforms}',
    COALESCE((SELECT jsonb_agg(platform || jsonb_build_object('pageUrl', COALESCE(platform->>'pageUrl', '')) ORDER BY position)
              FROM jsonb_array_elements(document->'platforms') WITH ORDINALITY AS entries(platform, position)), '[]'::jsonb)),
    revision = revision + 1,
    updated_at = now();
