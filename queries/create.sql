DROP TABLE "Account";

CREATE TABLE "Account" (
  "id"            BIGSERIAL,
  "email"         VARCHAR(60)  NOT NULL,
  "password"      VARCHAR(256) NOT NULL,
  "first_name"    VARCHAR(40)  NOT NULL,
  "last_name"     VARCHAR(20)  NOT NULL,
  "creation_date" TIMESTAMP    NOT NULL,
  "user_type"     VARCHAR(10) DEFAULT 'student',
  CONSTRAINT "accountPK" PRIMARY KEY ("id"),
  CONSTRAINT "emailUQ" UNIQUE ("email")
);

CREATE TABLE "Category" (
  "id"   BIGSERIAL,
  "name" VARCHAR(100) NOT NULL,
  CONSTRAINT "categoryPK" PRIMARY KEY ("id"),
  CONSTRAINT "nameUQ" UNIQUE ("name")
);

CREATE TABLE "Conversation" (
  "id"          BIGSERIAL,
  "account1_id" BIGINT NOT NULL,
  "account2_id" BIGINT NOT NULL,
  CONSTRAINT "conversationPK" PRIMARY KEY ("id"),
  CONSTRAINT "accountsUQ" UNIQUE ("account1_id", "account2_id"),
  CONSTRAINT "conversationAccountFK1" FOREIGN KEY ("account1_id")
  REFERENCES "Account" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "conversationAccountFK2" FOREIGN KEY ("account2_id")
  REFERENCES "Account" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "Lesson" (
  "id"           BIGSERIAL,
  "teacher_id"   BIGINT           NOT NULL,
  "category_id"  BIGINT           NOT NULL,
  "name"         VARCHAR(200)     NOT NULL,
  "price"        DOUBLE PRECISION NOT NULL,
  "creationDate" TIMESTAMP        NOT NULL,
  "description"  TEXT,
  "is_active"    BOOLEAN DEFAULT TRUE,
  CONSTRAINT "lessonPK" PRIMARY KEY ("id"),
  CONSTRAINT "priceCHK" CHECK ("price" > 0 AND "price" < 1000),
  CONSTRAINT "lessonUQ" UNIQUE ("teacher_id", "name"),
  CONSTRAINT "lessonAccountFK" FOREIGN KEY ("teacher_id")
  REFERENCES "Account" ("id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  CONSTRAINT "lessonCategoryFK" FOREIGN KEY ("category_id")
  REFERENCES "Category" ("id")
  ON DELETE RESTRICT
  ON UPDATE CASCADE
);

CREATE TABLE "Message" (
  "id"              BIGSERIAL,
  "sender_id"       BIGINT    NOT NULL,
  "receiver_id"     BIGINT    NOT NULL,
  "conversation_id" BIGINT    NOT NULL,
  "body"            TEXT,
  "date"            TIMESTAMP NOT NULL,
  CONSTRAINT "messagePK" PRIMARY KEY ("id"),
  CONSTRAINT "messageAccountFK1" FOREIGN KEY ("sender_id")
  REFERENCES "Account" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "messageAccountFK2" FOREIGN KEY ("receiver_id")
  REFERENCES "Account" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "messageConversationFK" FOREIGN KEY ("conversation_id")
  REFERENCES "Conversation" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "Report" (
  "id"          BIGSERIAL,
  "lesson_id"   BIGINT       NOT NULL,
  "student_id"  BIGINT       NOT NULL,
  "title"       VARCHAR(200) NOT NULL,
  "description" TEXT         NOT NULL,
  "is_resolved" BOOLEAN DEFAULT FALSE,
  "date"        TIMESTAMP    NOT NULL,
  CONSTRAINT "reportPK" PRIMARY KEY ("id"),
  CONSTRAINT "reportLessonFK" FOREIGN KEY ("lesson_id")
  REFERENCES "Lesson" ("id")
  ON DELETE RESTRICT
  ON UPDATE CASCADE,
  CONSTRAINT "reportAccountFK" FOREIGN KEY ("student_id")
  REFERENCES "Account" ("id")
  ON DELETE RESTRICT
  ON UPDATE CASCADE
);

CREATE TABLE "Review" (
  "id"         BIGSERIAL,
  "lesson_id"  BIGINT           NOT NULL,
  "student_id" BIGINT           NOT NULL,
  "rating"     DOUBLE PRECISION NOT NULL,
  "title"      VARCHAR(200)     NOT NULL,
  "comment"    TEXT DEFAULT '',
  "date"       TIMESTAMP        NOT NULL,
  CONSTRAINT "reviewPK" PRIMARY KEY ("id"),
  CONSTRAINT "reviewLessonFK" FOREIGN KEY ("lesson_id")
  REFERENCES "Lesson" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "reviewAccountFK" FOREIGN KEY ("student_id")
  REFERENCES "Account" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);

CREATE TABLE "Transaction" (
  "id"         BIGSERIAL,
  "lesson_id"  BIGINT    NOT NULL,
  "student_id" BIGINT    NOT NULL,
  "price"      INT       NOT NULL,
  "date"       TIMESTAMP NOT NULL,
  CONSTRAINT "transactionPK" PRIMARY KEY ("id"),
  CONSTRAINT "transactionLessonFK" FOREIGN KEY ("lesson_id")
  REFERENCES "Lesson" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE,
  CONSTRAINT "transactionAccountFK" FOREIGN KEY ("student_id")
  REFERENCES "Account" ("id")
  ON DELETE NO ACTION
  ON UPDATE CASCADE
);


