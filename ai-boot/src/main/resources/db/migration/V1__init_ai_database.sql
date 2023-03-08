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

INSERT INTO "ailpha_ai_analysis_algorithm"("algorithm_id", "algorithm_name", "description", "other", "paper", "patent")
VALUES ('ARIMA', 'ARIMA',
        '自回归积分滑动平均模型，将非平稳时间序列转化为平稳时间序列，然后将因变量仅对它的滞后值以及随机误差项的现值和滞后值进行回归所建立的模型。 AR是自回归, MA为移动平均。\n算法简称：ARIMA\n算法补充描述：\nAuto Regressive Integrated Moving Average\nARIMA（p，d，q），AR是自回归, p为自回归项; MA为移动平均，q为移动平均项数，d为时间序列成为平稳时所做的差分次数。\n优点： 模型十分简单，只需要内生变量而不需要借助其他外生变量。\n缺点：要求时序数据是稳定的（stationary），或者是通过差分化(differencing)后是稳定的。\n对预测值的作用，增加近期观察值的权重，同时可控制权重的变化速率。',
        '残差置信水平:\n    将真实序列和拟合序列作差之后得到残差序列，并假设残差序列为正态分布。根据此正态分布的均值和方差估计出残差置信水平。若某个点对应的残差置信水平越高，表示该点越异常。', null, null),
       ('ExponentialSmoothing', 'Exponential Smoothing',
        '指数平滑法常用于中短期趋势预测。是一种加权移动平均法。其特点是可加强观察期近期观察值对预测值的作用，增加近期观察值的权重，同时可控制权重的变化速率。\n算法简称：ES',
        '残差置信水平:\n    将真实序列和拟合序列作差之后得到残差序列，并假设残差序列为正态分布。根据此正态分布的均值和方差估计出残差置信水平。若某个点对应的残差置信水平越高，表示该点越异常。', null, null),
       ('RPCASST', 'RPCA-SST',
        '日常观测数据往往包含噪声干扰，该算法将时序数据片段转化为矩阵结构，利用RPCA重构矩阵剔除大幅值噪声，提高突变程度略低的异常点检测性能，发现掩盖在噪声下的异常信息。\n算法简称：RPCA-SST\n算法补充描述：\nRobust Principle Component Analysis based Singular Spectrum Transform\n优势\n1. 针对于序列当中广泛存在的稀疏大噪声情况，该方法较为鲁棒，即能在免除噪声干扰的同时对突变点进行检测；\n2. 该方法能抑制模型对高程度突变的过度反应，从侧面可以提高对突变程度略低的突变点的检测性能。\n对预测值的作用，增加近期观察值的权重，同时可控制权重的变化速率。',
        '突变点得分:\n    利用本算法检测出的异常点会给出对应的评分来量化异常的程度，若某个异常点对应的突变得分越高，表示该点越异常。', '[
         {
           "content": "A Robust Change-point Detection Method by Eliminating Sparse Noises",
           "info": ""
         }
       ]', null),
       ('WeeklyGaussianEstimation', 'Weekly Gaussian Estimation',
        '训练以一周时间为一个周期的呈规律性分布的时间序列数据，网站访问量、车流量等数据均满足该规律。任意时刻的数据与之前几周同时刻数据应该符合高斯分布，利用 3-sigma 准则进行异常检测。\n算法简称：WGE',
        '标准差偏离\n    根据拟合出的高斯分布的均值和方差，可计算出某个点相对于均值偏离的方差的倍数，此方差倍数即为标准差偏离。若某个点对应的标准差偏离越大，表示该点越异常。', null, '[
         {
           "content": "一种基于行为触发的防御链路耗尽型CC攻击的方法",
           "info": "201610369623.5"
         },
         {
           "content": "一种网络流量异常检测方法及系统",
           "info": "201710803213.1"
         }
       ]');
