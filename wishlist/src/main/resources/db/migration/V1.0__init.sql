CREATE TABLE IF NOT EXISTS wishes
(
    id          UUID        NOT NULL,
    title       VARCHAR(255),
    description VARCHAR(1000),
    url         VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL
        DEFAULT now(),
    wishlist_id UUID        NOT NULL,
    owner_id    UUID        NOT NULL,
    CONSTRAINT pk_wishes PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS wishlist
(
    id          UUID          NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(1000) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL
        DEFAULT now(),
    owner_id    UUID          NOT NULL,
    CONSTRAINT pk_wishlist PRIMARY KEY (id)
);

ALTER TABLE wishes
    ADD CONSTRAINT FK_WISHES_ON_WISHLIST FOREIGN KEY (wishlist_id) REFERENCES wishlist (id);