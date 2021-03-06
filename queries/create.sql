CREATE TABLE "user_credentials" (
  "id"            BIGSERIAL PRIMARY KEY,
  "email"         TEXT      NOT NULL UNIQUE,
  "password"      TEXT      NOT NULL,
  "creation_date" TIMESTAMP NOT NULL,
  "user_role"     TEXT      NOT NULL
);

CREATE TABLE "instructor_profiles" (
  "id"          BIGSERIAL PRIMARY KEY,
  "first_name"  TEXT,
  "last_name"   TEXT,
  "description" TEXT,
  "occupation"  TEXT,
  "image_link"  TEXT,
  "hourly_rate" DOUBLE PRECISION NOT NULL DEFAULT 0,
  "video_link"  TEXT,
  "user_id"     BIGINT           NOT NULL UNIQUE,
  CONSTRAINT "profilesCredentialsFK" FOREIGN KEY ("user_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

CREATE TABLE "main_topics" (
  "id"   BIGSERIAL PRIMARY KEY,
  "name" TEXT NOT NULL UNIQUE
);

CREATE TABLE "user_topics" (
  "id"        BIGSERIAL PRIMARY KEY,
  "name"      TEXT   NOT NULL UNIQUE,
  "parent_id" BIGINT NOT NULL,
  CONSTRAINT "topicsTopicsFK" FOREIGN KEY ("parent_id")
  REFERENCES "main_topics" ("id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

CREATE TABLE "instructor_topics" (
  "id"            BIGSERIAL PRIMARY KEY,
  "instructor_id" BIGINT NOT NULL,
  "topic_id"      BIGINT NOT NULL,
  CONSTRAINT "instopicsUQ" UNIQUE ("instructor_id", "topic_id"),
  CONSTRAINT "instopicsFK1" FOREIGN KEY ("instructor_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  CONSTRAINT "instopicsFK2" FOREIGN KEY ("topic_id")
  REFERENCES "user_topics" ("id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

CREATE TABLE "trainee_profiles" (
  "id"         BIGSERIAL PRIMARY KEY,
  "first_name" TEXT,
  "last_name"  TEXT,
  "image_link" TEXT,
  "user_id"    BIGINT NOT NULL UNIQUE,
  CONSTRAINT "profilesCredentialsFK" FOREIGN KEY ("user_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

CREATE TABLE "conversations" (
  "id"            BIGSERIAL PRIMARY KEY,
  "user1_id"      BIGINT  NOT NULL,
  "user2_id"      BIGINT  NOT NULL,
  "user1_removed" BOOLEAN NOT NULL DEFAULT FALSE,
  "user2_removed" BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT "accountsUQ" UNIQUE ("user1_id", "user2_id"),
  CONSTRAINT "conversationsCredentialsFK1" FOREIGN KEY ("user1_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "conversationsCredentialsFK2" FOREIGN KEY ("user2_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "messages" (
  "id"              BIGSERIAL PRIMARY KEY,
  "sender_id"       BIGINT    NOT NULL,
  "session_id" BIGINT    NOT NULL,
  "body"            TEXT      NOT NULL,
  "date"            TIMESTAMP NOT NULL,
  CONSTRAINT "messageAccountFK1" FOREIGN KEY ("sender_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "messageSessionFK" FOREIGN KEY ("session_id")
  REFERENCES "sessions" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "sessions" (
  "id"            BIGSERIAL PRIMARY KEY,
  "name"          TEXT      NOT NULL,
  "description"   TEXT      NOT NULL,
  "trainee_id"    BIGINT    NOT NULL,
  "instructor_id" BIGINT    NOT NULL,
  "topic_id"      BIGINT    NOT NULL,
  "date"          TIMESTAMP NOT NULL,
  "is_approved"   BOOLEAN   NOT NULL DEFAULT FALSE,
  "is_completed"  BOOLEAN   NOT NULL DEFAULT FALSE,
  "is_deleted"    BOOLEAN   NOT NULL DEFAULT FALSE,
  "is_started"    BOOLEAN   NOT NULL DEFAULT FALSE,
  CONSTRAINT "sessionsTopicsFK" FOREIGN KEY ("topic_id")
  REFERENCES "user_topics" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "sessionsCredentialsFK1" FOREIGN KEY ("trainee_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "sessionsCredentialsFK2" FOREIGN KEY ("instructor_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "reports" (
  "id"          BIGSERIAL PRIMARY KEY,
  "session_id"  BIGINT    NOT NULL,
  "user_id"     BIGINT    NOT NULL,
  "title"       TEXT      NOT NULL,
  "description" TEXT      NOT NULL,
  "is_resolved" BOOLEAN   NOT NULL DEFAULT FALSE,
  "date"        TIMESTAMP NOT NULL,
  CONSTRAINT "reportsSessionsFK" FOREIGN KEY ("session_id")
  REFERENCES "sessions" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "reportsCredentialsFK" FOREIGN KEY ("user_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "reviews" (
  "id"         BIGSERIAL PRIMARY KEY,
  "session_id" BIGINT           NOT NULL,
  "trainee_id" BIGINT           NOT NULL,
  "rating"     DOUBLE PRECISION NOT NULL,
  "title"      TEXT             NOT NULL,
  "comment"    TEXT,
  "date"       TIMESTAMP        NOT NULL,
  CONSTRAINT "reviewsUQ" UNIQUE ("trainee_id", "session_id"),
  CONSTRAINT "ratingChk" CHECK ("rating" >= 0 AND "rating" <= 5),
  CONSTRAINT "reviewsSessionsFK" FOREIGN KEY ("session_id")
  REFERENCES "sessions" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "reviewsCredentialsFK" FOREIGN KEY ("trainee_id")
  REFERENCES "user_credentials" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "session_files" (
  "id"         BIGSERIAL PRIMARY KEY,
  "session_id" BIGINT NOT NULL,
  "name"       TEXT   NOT NULL,
  "link"       TEXT   NOT NULL,
  CONSTRAINT "sessionFilesFK" FOREIGN KEY ("session_id")
  REFERENCES "sessions" ("id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

-- ADD MORE STUFF
-- CREATE TABLE "notifications" (
--   "id" BIGSERIAL PRIMARY KEY,
--   "notif_type" TEXT NOT NULL,
--   "is_read" BOOLEAN NOT NULL DEFAULT FALSE
-- )

INSERT INTO "main_topics" ("name") VALUES
  ('Business'),
  ('Office Productivity'),
  ('Personal Development'),
  ('Music'),
  ('Design'),
  ('Marketing'),
  ('Lifestyle'),
  ('Photography'),
  ('Health & Fitness'),
  ('Teacher Training'),
  ('Academics'),
  ('Language');
