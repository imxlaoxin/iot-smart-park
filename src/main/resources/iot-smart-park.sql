use `iot-intelligent-park`;

create table park_env_info
(
    id                     bigint auto_increment comment '唯一标识' primary key,
    temperature            varchar(10) null comment '停车场空气温度',
    humidity               int         null comment '停车场空气湿度',
    light_intensity        varchar(10) null comment '停车场光照强度',
    smoke_density          int         null comment '停车场烟雾浓度',
    carbon_dioxide_density int         null comment '停车场二氧化碳浓度',
    create_time            datetime    null comment '创建时间',
    update_time            datetime    null comment '更新时间'
) comment '停车场环境相关信息';

create table charging_pile_info
(
    id            bigint auto_increment comment '唯一标识' primary key,
    charger_id    int      null comment '充电桩标识',
    temperature   int      null comment '充电桩内部温度',
    humidity      int      null comment '充电桩内部空气湿度',
    current       int      null comment '充电桩电流',
    voltage       int      null comment '充电桩电压',
    status        tinyint  null comment '充电桩状态(0: 停止; 1: 运行; 2: 异常)',
    charge_status tinyint  null comment '充电状态(0: 关闭; 1: 开启)',
    create_time   datetime null comment '创建时间',
    update_time   datetime null comment '更新时间'
) comment '充电桩相关信息';

create table env_exp_info
(
    id          bigint auto_increment comment '唯一标识' primary key,
    env_type    tinyint     null comment '环境类型(0: 温度; 1: 湿度; 2: 烟雾; 3: CO2; 4: 光强)',
    level       tinyint     null comment '告警等级(0: 预警 1: 危险; 2: 危急)',
    exp_info    varchar(50) null comment '异常信息',
    ai_analysis text        null comment '大模型分析与处置建议',
    create_time datetime    null comment '创建时间',
    update_time datetime    null comment '更新时间'
) comment '环境异常警报信息表';

create table charger_exp_info
(
    id          bigint auto_increment comment '唯一标识' primary key,
    charger_id  int         null comment '充电桩标识',
    exp_info    varchar(50) not null comment '异常信息',
    create_time datetime    null comment '创建时间',
    update_time datetime    null comment '更新时间'
) comment '充电桩异常信息表';

create table biz_detect_record
(
    id            bigint auto_increment primary key,
    cam_id        bigint       not null comment '设备ID',
    detect_type   varchar(32)  not null comment '识别类型: car, license-plate, person',
    detect_result varchar(255) null comment '结构化结果(如: 车牌号"粤A88888"或人数"5")',
    confidence    float        null comment '置信度',
    original_url  varchar(500) null comment 'MinIO原始图URL',
    detect_url    varchar(500) null comment 'MinIO标注图URL',
    create_time   datetime     null comment '识别时间',
    update_time   datetime     null comment '更新时间'
) comment '常规业务识别记录表';

create table exp_detect_record
(
    id             bigint auto_increment primary key,
    cam_id         bigint       not null comment '设备ID',
    alarm_type     varchar(32)  not null comment '告警类型: fire, smoke',
    alarm_level    tinyint      null comment '告警级别: 0-预警, 1-危险, 2-危急',
    original_url   varchar(500) null comment 'MinIO原始图URL',
    detect_url     varchar(500) null comment 'MinIO标注图URL',
    confidence     float        null comment '置信度',
    process_status tinyint      null comment '处理状态: 0-未处理, 1-确认属实, 2-误报, 3-已解决',
    handler_remark varchar(255) null comment '处理意见/备注',
    create_time    datetime     null comment '发生时间',
    update_time    datetime     null comment '处理时间'
) comment '火灾/紧急告警记录表';

create table park_spot_info
(
    id          bigint auto_increment comment '唯一标识' primary key,
    spot_code   varchar(20) not null comment '车位编号(如: P1, P2)',
    status      tinyint     not null default 0 comment '车位状态(0: 空闲, 1: 占用, 2: 故障)',
    sensor_id   varchar(50) null comment '绑定的硬件传感器ID/MAC地址',
    create_time datetime    null comment '创建时间',
    update_time datetime    null comment '状态最后更新时间'
) comment '停车场实时车位状态表';

create table park_billing_record
(
    id               bigint auto_increment comment '订单ID' primary key,
    license_plate    varchar(20)    not null comment '车牌号',
    plate_color      varchar(10)    null comment '车牌颜色',
    entry_time       datetime       not null comment '入场时间',
    exit_time        datetime       null comment '出场时间',
    parking_duration int            null comment '停车总时长(分钟)',
    total_fee        decimal(10, 2) null comment '应收费用(元)',
    order_status     tinyint        not null default 0 comment '订单状态(0: 停车中, 1: 待缴费, 2: 已完成)',
    create_time      datetime       null comment '创建时间',
    update_time      datetime       null comment '更新时间'
) comment '停车计费订单记录表';

create table charging_billing_record
(
    id              bigint auto_increment comment '充电订单ID' primary key,
    charger_id      int            not null comment '充电桩标识(关联 charging_pile_info)',
    park_billing_id bigint         null comment '关联的停车订单ID(用于出口合并结算)',
    license_plate   varchar(20)    null comment '车牌号(可选，用于账单追溯)',
    start_time      datetime       not null comment '开始充电时间',
    end_time        datetime       null comment '结束充电时间',
    power_consumed  decimal(8, 2)  null comment '累计消耗电量(度/kWh)',
    total_fee       decimal(10, 2) null comment '充电总费用(元)',
    order_status    tinyint        not null default 0 comment '订单状态(0: 充电中, 1: 待缴费, 2: 已完成)',
    create_time     datetime       null comment '创建时间',
    update_time     datetime       null comment '更新时间'
) comment '充电计费订单记录表';

create table billing_rule
(
    id          int auto_increment comment '规则ID' primary key,
    rule_code   varchar(50)    not null comment '规则代码(如: PARKING_FEE_PER_HOUR)',
    rule_name   varchar(50)    not null comment '规则名称(如: 停车费单价/小时)',
    rule_value  decimal(10, 2) not null comment '规则数值(如: 5.00)',
    unit        varchar(20)    null comment '单位(如: 元/小时, 元/度)',
    description varchar(255)   null comment '规则描述及备注',
    status      tinyint default 1 comment '状态(1:启用, 0:停用)',
    create_time datetime       null comment '创建时间',
    update_time datetime       null comment '最后更新时间'
) comment '停车场计费规则配置表';
