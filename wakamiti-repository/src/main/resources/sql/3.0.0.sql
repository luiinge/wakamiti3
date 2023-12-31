CREATE TABLE PLAN_NODE(
    NODE_ID UUID NOT NULL,
    TYPE TINYINT NOT NULL,
    NAME VARCHAR(300),
    LANGUAGE VARCHAR(10),
    IDENTIFIER VARCHAR(100),
    SOURCE VARCHAR(300),
    KEYWORD VARCHAR(100),
    DESCRIPTION CLOB,
    DISPLAY_NAME_PATTERN VARCHAR(100),
    DATA_TABLE CLOB,
    DOCUMENT CLOB,
    DOCUMENT_TYPE VARCHAR(100),
    PRIMARY KEY (NODE_ID)
);
CREATE TABLE PLAN_NODE_TAG(
    NODE_ID UUID NOT NULL,
    TAG VARCHAR(100) NOT NULL,
    PRIMARY KEY (NODE_ID, TAG),
    FOREIGN KEY (NODE_ID) REFERENCES PLAN_NODE(NODE_ID) ON DELETE CASCADE
);
CREATE TABLE PLAN_NODE_PROPERTY(
    NODE_ID UUID NOT NULL,
    PROPERTY_KEY VARCHAR(100) NOT NULL,
    PROPERTY_VALUE VARCHAR(100),
    PRIMARY KEY (NODE_ID, PROPERTY_KEY),
    FOREIGN KEY (NODE_ID) REFERENCES PLAN_NODE(NODE_ID) ON DELETE CASCADE
);
CREATE VIEW V_PLAN_NODE	AS
    SELECT N.*,
    ARRAY(SELECT TAG FROM PLAN_NODE_TAG T WHERE T.NODE_ID = N.NODE_ID) AS TAGS,
    ARRAY(SELECT PROPERTY_KEY||'='||PROPERTY_VALUE FROM PLAN_NODE_PROPERTY P WHERE P.NODE_ID = N.NODE_ID) AS PROPERTIES
    FROM PLAN_NODE N
;
CREATE TABLE PLAN_NODE_HIERARCHY(
    NODE_ID UUID NOT NULL,
    ROOT UUID NOT NULL,
    PATH UUID ARRAY,
    SIBLING_ORDER SMALLINT NOT NULL,
    PRIMARY KEY (NODE_ID),
    FOREIGN KEY (NODE_ID) REFERENCES PLAN_NODE(NODE_ID) ON DELETE CASCADE,
    FOREIGN KEY (ROOT) REFERENCES PLAN_NODE(NODE_ID) ON DELETE CASCADE
);
CREATE INDEX IDX_PLAN_NODE_HIERARCHY_ROOT ON PLAN_NODE_HIERARCHY(ROOT);
CREATE TABLE PLAN (
    PLAN_ID UUID NOT NULL,
    ORGANIZATION VARCHAR(100),
    PROJECT VARCHAR(100),
    NAME VARCHAR(300),
    HASH CHAR(64) NOT NULL,
    ROOT_NODE UUID,
    TAG_FILTER VARCHAR(300),
    PRIMARY KEY (PLAN_ID),
    FOREIGN KEY (ROOT_NODE) REFERENCES PLAN_NODE(NODE_ID) ON DELETE CASCADE
);