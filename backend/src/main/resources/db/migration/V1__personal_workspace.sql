CREATE TABLE workspace (
    id integer PRIMARY KEY CHECK (id = 1),
    revision bigint NOT NULL DEFAULT 0,
    document jsonb NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT now()
);
INSERT INTO workspace (id, document) VALUES (1, '{"contents":[],"tags":[],"platforms":[{"id":"facebook","name":"Facebook","active":true},{"id":"instagram","name":"Instagram","active":true},{"id":"tiktok","name":"TikTok","active":true},{"id":"youtube","name":"YouTube","active":true}],"timezone":"Asia/Bangkok"}');
