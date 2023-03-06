CREATE TABLE "ailpha_ai_analysis_algorithm"
(
    "algorithm_id"   varchar(100) NOT NULL,
    "algorithm_name" varchar(100) NOT NULL,
    "description"    text         NOT NULL,
    "other"          text,
    "paper"          jsonb,
    "patent"         jsonb,
    PRIMARY KEY (algorithm_id)
);
COMMENT ON COLUMN "ailpha_ai_analysis_algorithm".algorithm_id IS '算法id';
COMMENT ON COLUMN "ailpha_ai_analysis_algorithm".algorithm_name IS '算法名称';
COMMENT ON COLUMN "ailpha_ai_analysis_algorithm"."other" IS '误差描述';
COMMENT ON COLUMN "ailpha_ai_analysis_algorithm"."paper" IS '论文(格式[{"content":"论文描述","info":""}])';
COMMENT ON COLUMN "ailpha_ai_analysis_algorithm"."patent" IS '专利(格式[{"content":"专利描述","info":"专利号"}])';

CREATE TABLE "ailpha_ai_analysis_data"
(
    "id"            varchar(100) NOT NULL,
    "original_data" text         NOT NULL,
    "ui_data"       text         NOT NULL,
    "create_time"   int8,
    "model_id"      varchar(100) NOT NULL,
    "algorithm_id"  varchar(100) NOT NULL,
    CONSTRAINT "data_pk_id" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "ailpha_ai_analysis_data"."id" IS '模型数据id';
COMMENT ON COLUMN "ailpha_ai_analysis_data".original_data IS '分析结果原始数据';
COMMENT ON COLUMN "ailpha_ai_analysis_data".ui_data IS '前端展示结果数据';
COMMENT ON COLUMN "ailpha_ai_analysis_data".model_id IS '模型id';
COMMENT ON COLUMN "ailpha_ai_analysis_data".algorithm_id IS '算法id';

CREATE TABLE "ailpha_ai_analysis_scene"
(
    "scene_id"     varchar(100) NOT NULL,
    "scene_no"     int4         NOT NULL,
    "model_id"     varchar(100) NOT NULL,
    "algorithm_id" varchar(100),
    "is_enable"    varchar(10),
    "other"        text,
    CONSTRAINT "scene_pk_id" PRIMARY KEY (scene_id)
);
COMMENT ON COLUMN "ailpha_ai_analysis_scene".scene_id IS '场景id';
COMMENT ON COLUMN "ailpha_ai_analysis_scene".scene_no IS '场景号';
COMMENT ON COLUMN "ailpha_ai_analysis_scene"."model_id" IS '模型id';
COMMENT ON COLUMN "ailpha_ai_analysis_scene"."algorithm_id" IS '算法id';
COMMENT ON COLUMN "ailpha_ai_analysis_scene".is_enable IS '是否启用';
COMMENT ON COLUMN "ailpha_ai_analysis_scene"."other" IS '其他';

