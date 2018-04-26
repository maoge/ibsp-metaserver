/*
Integrated basic service platform
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

USE `ibsp`;

/*Table structure for attribute table `t_meta_attr` */
DROP TABLE IF EXISTS `t_meta_attr`;
CREATE TABLE `t_meta_attr` (
  `ATTR_ID`       int         NOT NULL COMMENT '属性ID',
  `ATTR_NAME`     varchar(48) NOT NULL COMMENT '属性名字(EN)',
  `ATTR_NAME_CN`  varchar(72) NOT NULL COMMENT '属性名字(CN)',
  `AUTO_GEN`      char(1)     NOT NULL COMMENT '0:非自动生成;1:自动生成',
  PRIMARY KEY (`ATTR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `t_meta_attr`(`ATTR_ID`,`ATTR_NAME`,`ATTR_NAME_CN`, `AUTO_GEN`) values 
(100, 'IP',                                'IP',                      '0'),
(101, 'PORT',                              '服务端口',                '0'),
(102, 'MGR_PORT',                          '管理端口',                '0'),
(103, 'SYNC_PORT',                         '同步端口',                '0'),
(104, 'STAT_PORT',                         '统计数据端口',            '0'),
(105, 'COLLECTD_ID',                       'COLLECTD ID',             '0'),
(106, 'COLLECTD_NAME',                     'COLLECTD NAME',           '0'),
(107, 'CLUSTER_PORT',                      '集群端口',                '0'),
(108, 'OS_USER',                           '系统账户',                '0'),
(109, 'OS_PWD',                            '系统密码',                '0'),
(200, 'MQ_SVC_CONTAINER_ID',               'MQ服务容器ID',            '1'),
(201, 'MQ_SVC_CONTAINER_NAME',             'MQ服务容器名字',          '0'),
(202, 'VBROKER_CONTAINER_ID',              'VBroker容器ID',           '1'),
(203, 'VBROKER_CONTAINER_NAME',            'VBroker容器名字',         '0'),
(204, 'MQ_SWITCH_CONTAINER_ID',            'MQSwitch容器ID',          '1'),
(205, 'MQ_SWITCH _CONTAINER_NAME',         'MQSwitch容器名字',        '0'),
(206, 'VBROKER_ID',                        'VBROKER ID',              '1'),
(207, 'VBROKER_NAME',                      'VBROKER NAME',            '0'),
(208, 'MASTER_ID',                         '主节点ID',                '0'),
(209, 'BROKER_ID',                         'BROKER ID',               '1'),
(210, 'BROKER_NAME',                       'BROKER NAME',             '0'),
(211, 'MQ_SWITCH_ID',                      'MQ Switch ID',            '1'),
(212, 'MQ_SWITCH_NAME',                    'MQ Switch Name',          '0'),
(213, 'CACHE_SVC_CONTAINER_ID',            'Cache服务容器ID',         '1'),
(214, 'CACHE_SVC_CONTAINER_NAME',          'Cache服务容器名字',       '0'),
(215, 'CACHE_PROXY_CONTAINER_ID',          'Cache proxy容器ID',       '1'),
(216, 'CACHE_PROXY_CONTAINER_NAME',        'Cache proxy容器名字',     '0'),
(217, 'CACHE_NODE_CONTAINER_ID',           'Cache node容器ID',        '1'),
(218, 'CACHE_NODE_CONTAINER_NAME',         'Cache node容器名字',      '0'),
(219, 'CACHE_NODE_CLUSTER_CONTAINER_ID',   'Cache Node Cluster ID',   '1'),
(220, 'CACHE_NODE_CLUSTER_CONTAINER_NAME', 'Cache Node Cluster 名字', '0'),
(221, 'CACHE_PROXY_ID',                    'Cache Proxy ID',          '1'),
(222, 'CACHE_PROXY_NAME',                  'Cache Proxy Name',        '0'),
(223, 'CACHE_NODE_ID',                     'Cache Node ID',           '1'),
(224, 'CACHE_NODE_NAME',                   'Cache Node Name',         '0'),
(225, 'DB_SVC_CONTAINER_ID',               'DB服务容器ID',            '1'),
(226, 'DB_SVC_CONTAINER_NAME',             'DB服务容器名字',          '0'),
(227, 'TIDB_CONTAINER_ID',                 'TIDB容器ID',              '1'),
(228, 'TIDB_CONTAINER_NAME',               'TIDB容器名字',            '0'),
(229, 'TIKV_CONTAINER_ID',                 'TIKV容器ID',              '1'),
(230, 'TIKV_CONTAINER_NAME',               'TIKV容器名字',            '0'),
(231, 'PD_CONTAINER_ID',                   'PD容器ID',                '1'),
(232, 'PD_CONTAINER_NAME',                 'PD容器名字',              '0'),
(233, 'PD_ID',                             'PD ID',                   '1'),
(234, 'PD_NAME',                           'PD Name',                 '0'),
(235, 'TIDB_ID',                           'TIDB ID',                 '1'),
(236, 'TIDB_NAME',                         'TIDB Name',               '0'),
(237, 'TIKV_ID',                           'TIKV ID',                 '1'),
(238, 'TIKV_NAME',                         'TIKV Name',               '0'),
(239, 'CACHE_SLOT',                        'Cache分片信息',           '0'),
(240, 'HOST_NAME',                         'host name',              '0'),
(241, 'ERL_COOKIE',                        'erlang cookie',          '0'),
(242, 'ROOT_PWD',                          'root密码',                '0'),
(243, 'MAX_MEMORY',                        '最大内存限制',                '0'),
(244, 'RW_SEPARATE',                       '读写分离',                 '0');


/*Table structure for component table `t_meta_cmpt` */
DROP TABLE IF EXISTS `t_meta_cmpt`;
CREATE TABLE `t_meta_cmpt` (
  `CMPT_ID`        int         NOT NULL COMMENT '组件ID',
  `CMPT_NAME`      varchar(48) NOT NULL COMMENT '组件名字(EN)',
  `CMPT_NAME_CN`   varchar(72) NOT NULL COMMENT '组件名字(CN)',
  `IS_NEED_DEPLOY` varchar(72) NOT NULL COMMENT '是否需要部署 0:不需要,1:需要',
  `SERV_CLAZZ`     varchar(16) NOT NULL COMMENT '服务分类',
  `SERV_TYPE`      varchar(32) NOT NULL COMMENT '组件类别',
  `SUB_SERV_TYPE`  varchar(256) NOT NULL COMMENT '子组件类别',
  PRIMARY KEY (`CMPT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `t_meta_cmpt`(`CMPT_ID`,`CMPT_NAME`,`CMPT_NAME_CN`,`IS_NEED_DEPLOY`,`SERV_CLAZZ`,`SERV_TYPE`,`SUB_SERV_TYPE`) values 
(100, 'MQ_SERV_CONTAINER',     'MQ服务容器',             '0', 'MQ',    'MQ_SERV_CONTAINER',     'MQ_VBROKER_CONTAINER,MQ_SWITCH_CONTAINER,MQ_COLLECTD'),
(101, 'MQ_VBROKER_CONTAINER',  'VBroker容器',            '0', 'MQ',    'MQ_VBROKER_CONTAINER',  'MQ_VBROKER'),
(102, 'MQ_SWITCH_CONTAINER',   'MQSwitch容器',           '0', 'MQ',    'MQ_SWITCH_CONTAINER',   'MQ_SWITCH'),
(103, 'MQ_VBROKER',            'VBroker',                '1', 'MQ',    'MQ_VBROKER',            'MQ_BROKER'),
(104, 'MQ_BROKER',             'Broker',                 '1', 'MQ',    'MQ_BROKER',             ''),
(105, 'MQ_SWITCH',             'MQ交换机',               '1', 'MQ',    'MQ_SWITCH',             ''),
(106, 'MQ_COLLECTD',           'MQ采集器',               '1', 'MQ',    'MQ_COLLECTD',           ''),       
(107, 'CACHE_SERV_CONTAINER',  'Cache服务容器',          '0', 'CACHE', 'CACHE_SERV_CONTAINER',  'CACHE_PROXY_CONTAINER,CACHE_NODE_CONTAINER,CACHE_COLLECTD'),
(108, 'CACHE_PROXY_CONTAINER', 'Cache Proxy容器',        '0', 'CACHE', 'CACHE_PROXY_CONTAINER', 'CACHE_PROXY'),
(109, 'CACHE_NODE_CONTAINER',  'Cache Node Cluster容器', '0', 'CACHE', 'CACHE_NODE_CONTAINER',  'CACHE_NODE_CLUSTER'),
(110, 'CACHE_NODE_CLUSTER',    'Cache Node Cluster',     '1', 'CACHE', 'CACHE_NODE_CLUSTER',    'CACHE_NODE'),
(111, 'CACHE_NODE',            'Cache Node',             '1', 'CACHE', 'CACHE_NODE',            ''),
(112, 'CACHE_PROXY',           'Cache接入机',            '1', 'CACHE', 'CACHE_PROXY',           ''),
(113, 'CACHE_COLLECTD',        'Cache采集器',            '1', 'CACHE', 'CACHE_COLLECTD',        ''),
(114, 'DB_SERV_CONTAINER',     'DB服务容器',             '0', 'DB',    'DB_SERV_CONTAINER',     'DB_TIDB_CONTAINER,DB_TIKV_CONTAINER,DB_PD_CONTAINER,DB_COLLECTD'),
(115, 'DB_TIDB_CONTAINER',     'TiDB-server容器',        '0', 'DB',    'DB_TIDB_CONTAINER',     'DB_TIDB'),
(116, 'DB_TIKV_CONTAINER',     'TiKV-server容器',        '0', 'DB',    'DB_TIKV_CONTAINER',     'DB_TIKV'),
(117, 'DB_PD_CONTAINER',       'PD-server容器',          '0', 'DB',    'DB_PD_CONTAINER',       'DB_PD'),
(118, 'DB_PD',                 'PD服务器',               '1', 'DB',    'DB_PD',                 ''),
(119, 'DB_TIDB',               'TiDB服务器',             '1', 'DB',    'DB_TIDB',               ''),
(120, 'DB_TIKV',               'TiKV服务器',             '1', 'DB',    'DB_TIKV',               ''),
(121, 'DB_COLLECTD',           'DB采集器',               '1', 'DB',    'DB_COLLECTD',           '');

/*Table structure for component-attribute table `t_meta_cmpt_attr` */
DROP TABLE IF EXISTS `t_meta_cmpt_attr`;
CREATE TABLE `t_meta_cmpt_attr` (
  `CMPT_ID`       int         NOT NULL COMMENT '组件ID',
  `ATTR_ID`       int         NOT NULL COMMENT '属性ID',
  UNIQUE KEY `IDX_CMPT_ATTR` (`CMPT_ID`,`ATTR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `t_meta_cmpt_attr`(`CMPT_ID`,`ATTR_ID`) values
(100, 200),
(100, 201),
(101, 202),
(101, 203),
(102, 204),
(102, 205),
(103, 206),
(103, 207),
(103, 208),
(104, 209),
(104, 210),
(104, 100),
(104, 101),
(104, 102),
(104, 103),
(104, 108),
(104, 109),
(104, 241),
(105, 211),
(105, 212),
(105, 100),
(105, 101),
(105, 102),
(105, 108),
(105, 109),
(106, 105),
(106, 106),
(106, 100),
(106, 101),
(106, 108),
(106, 109),
(107, 213),
(107, 214),
(108, 215),
(108, 216),
(109, 217),
(109, 218),
(110, 219),
(110, 220),
(110, 239),
(110, 208),
(110, 243),
(111, 223),
(111, 224),
(111, 100),
(111, 101),
(111, 108),
(111, 109),
(112, 221),
(112, 222),
(112, 100),
(112, 101),
(112, 104),
(112, 108),
(112, 109),
(113, 105),
(113, 106),
(113, 100),
(113, 101),
(113, 108),
(113, 109),
(114, 225),
(114, 226),
(115, 227),
(115, 228),
(116, 229),
(116, 230),
(117, 231),
(117, 232),
(118, 233),
(118, 234),
(118, 100),
(118, 101),
(118, 107),
(118, 108),
(118, 109),
(119, 235),
(119, 236),
(119, 100),
(119, 101),
(119, 104),
(119, 108),
(119, 109),
(120, 237),
(120, 238),
(120, 100),
(120, 101),
(120, 108),
(120, 109),
(121, 105),
(121, 106),
(121, 100),
(121, 101),
(121, 108),
(121, 109);


/*---------------采集指标编码对照表----------------*/
DROP TABLE IF EXISTS `t_meta_collect_quota`;
CREATE TABLE `t_meta_collect_quota` (
  `QUOTA_CODE`    int         NOT NULL COMMENT '采集指标编码',
  `QUOTA_NAME`    varchar(48) NOT NULL COMMENT '采集指标名称',
  PRIMARY KEY (`QUOTA_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `t_meta_collect_quota`(`QUOTA_CODE`,`QUOTA_NAME`) values
( 1, 'CPU.Used'),
( 2, 'MEM.Used'),
( 3, 'DISK.Total'),
( 4, 'DISK.Used'),
( 5, 'DISK.Available');

/*---------------组件实例----------------*/
DROP TABLE IF EXISTS `t_instance`;
CREATE TABLE `t_instance` (
  `INST_ID`       varchar(36) NOT NULL COMMENT '实例ID',
  `CMPT_ID`       int         NOT NULL COMMENT '组件ID',
  `IS_DEPLOYED`   varchar(1)  NOT NULL COMMENT '0:未部署;1:已部署',
  `POS_X`         int         NOT NULL COMMENT '组件左上顶点X坐标',
  `POS_Y`         int         NOT NULL COMMENT '组件左上顶点Y坐标',
  `WIDTH`         int         default -1 COMMENT '组件宽度',
  `HEIGHT`        int         default -1 COMMENT '组件高度',
  `ROW`           int         default -1 COMMENT 'layout row',
  `COL`           int         default -1 COMMENT 'layout column',
  PRIMARY KEY (`INST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*----------------实例属性----------------*/
DROP TABLE IF EXISTS `t_instance_attr`;
CREATE TABLE `t_instance_attr` (
  `INST_ID`       varchar(36) NOT NULL COMMENT '实例ID',
  `ATTR_ID`       int         NOT NULL COMMENT '属性ID',
  `ATTR_NAME`     varchar(48) NOT NULL COMMENT '属性key',
  `ATTR_VALUE`    varchar(72) NOT NULL COMMENT '属性value',
  UNIQUE KEY `IDX_INSTANCE_INST_ATTR_ID` (`INST_ID`,`ATTR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*----------------部署服务----------------*/
DROP TABLE IF EXISTS `t_service`;
CREATE TABLE `t_service` (
  `INST_ID`       varchar(36) NOT NULL COMMENT '服务ID,即最外层的容器ID',
  `SERV_NAME`     varchar(32) NOT NULL COMMENT '服务名字',
  `SERV_TYPE`     varchar(16) NOT NULL COMMENT '服务类型:MQ,CACHE,DB',
  `IS_DEPLOYED`   varchar(1)  NOT NULL COMMENT '0:未部署;1:已部署',
  `IS_PRODUCT`    varchar(1)  NOT NULL COMMENT '0:非生产;1:生产',
  `CREATE_TIME`   bigint(14)  NOT NULL COMMENT '创建时间',
  `USER`          varchar(32) DEFAULT NULL COMMENT '服务默认用户',
  `PASSWORD`      varchar(64) DEFAULT NULL COMMENT '服务默认密码',
  PRIMARY KEY (`INST_ID`),
  UNIQUE KEY `SERV_NAME` (`SERV_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*----------------拓扑关系----------------*/
DROP TABLE IF EXISTS `t_topology`;
CREATE TABLE `t_topology` (
  `INST_ID1`     varchar(36) NOT NULL COMMENT 'A端INST_ID或父INST_ID',
  `INST_ID2`     varchar(36) NOT NULL COMMENT 'Z端INST_ID或子INST_ID',
  `TOPO_TYPE`    tinyint     NOT NULL COMMENT 'TOPO类型:1 link;2 contain',
  KEY `IDX_TOPO_INST_ID1` (`INST_ID1`),
  KEY `IDX_TOPO_INST_ID2` (`INST_ID2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*----------------自动部署----------------*/
DROP TABLE IF EXISTS `t_ftp_host`;
CREATE TABLE `t_ftp_host` (
  `HOST_ID`      varchar(16) NOT NULL COMMENT '主机标识ID',
  `IP_ADDRESS`   varchar(16) NOT NULL COMMENT 'IP地址',
  `USER_NAME`    varchar(32) NOT NULL COMMENT '用户名',
  `USER_PWD`     varchar(32) NOT NULL COMMENT '密码',
  `FTP_PORT`     varchar(8)  NOT NULL COMMENT 'ftp端口',
  `CREATE_TIME`  bigint(14)  NOT NULL COMMENT '添加时间',
  PRIMARY KEY (`HOST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert  into `t_ftp_host`(`HOST_ID`,`IP_ADDRESS`,`USER_NAME`,`USER_PWD`,`FTP_PORT`,`CREATE_TIME`) values 
('1', '192.168.14.206', 'mq1', 'amqp1', '22', 1456105739394);


DROP TABLE IF EXISTS `t_file_deploy`;
CREATE TABLE `t_file_deploy` (
  `FILE_ID`      varchar(32)  NOT NULL COMMENT '文件ID',
  `HOST_ID`      varchar(16)  NOT NULL COMMENT '主机标识ID',
  `FILE_TYPE`    varchar(32)  NOT NULL COMMENT '文件类型',
  `SERV_CLAZZ`   varchar(32)  NOT NULL COMMENT '服务分类',
  `FILE_NAME`    varchar(256) NOT NULL COMMENT '文件名',
  `FILE_DIR`     varchar(255) NOT NULL COMMENT '文件所在目录',
  `CREATE_TIME`  bigint(14)   NOT NULL COMMENT '添加时间',
  PRIMARY KEY (`FILE_ID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT  INTO `t_file_deploy`(`FILE_ID`,`HOST_ID`,`FILE_TYPE`,`SERV_CLAZZ`,`FILE_NAME`,`FILE_DIR`,`CREATE_TIME`) VALUES 
('1', '1', 'DB_TIDB',     'DB', 'tidb_server-2.0.0.tar.gz',                  '/home/mq1/ftp/', 1456105739394),
('2', '1', 'DB_TIKV',     'DB', 'tikv_server-2.0.0.tar.gz',                  '/home/mq1/ftp/', 1456105739394),
('3', '1', 'DB_PD',       'DB', 'pd_server-2.0.0.tar.gz',                    '/home/mq1/ftp/', 1456105739394),
('4', '1', 'COLLECTD',    '',   'collectd-1.0.0.tar.gz',                     '/home/mq1/ftp/', 1456105739394),
('5', '1', 'MQ_RABBIT',   'MQ', 'rabbitmq-server-generic-unix-3.4.3.tar.gz', '/home/mq1/ftp/', 1456105739394),
('6', '1', 'MQ_ERLANG',   'MQ', 'otp_R15B.tar.gz',                           '/home/mq1/ftp/', 1456105739394),
('7', '1', 'CACHE_PROXY', 'CACHE', 'cache_proxy-1.2.0.tar.gz',               '/home/mq1/ftp/', 1456105739394),
('8', '1', 'CACHE_NODE',  'CACHE', 'redis_ffcs-2.8.19.tar.gz',               '/home/mq1/ftp/', 1456105739394),
('9', '1', 'JDK',         '',   'jdk1.8.0_72.tar.gz',                        '/home/mq1/ftp/', 1456105739394);

DROP TABLE IF EXISTS `t_monitor_history`;
CREATE TABLE `t_monitor_history` (
  `INST_ID`      varchar(36) NOT NULL,
  `TS`           bigint(14)  NOT NULL COMMENT '采集时间',
  `QUOTA_CODE`   tinyint     NOT NULL COMMENT '指标编码',
  `QUOTA_MEAN`   varchar(16) NOT NULL COMMENT '指标值',
  KEY `IDX_MONITOR_COLLECT` (`INST_ID`,`TS`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_monitor_current`;
CREATE TABLE `t_monitor_current` (
  `INST_ID`      varchar(36) NOT NULL,
  `TS`           bigint(14)  NOT NULL COMMENT '采集时间',
  `QUOTA_CODE`   tinyint     NOT NULL COMMENT '指标编码',
  `QUOTA_MEAN`   varchar(16) NOT NULL COMMENT '指标值',
  KEY `IDX_MONITOR_COLLECT` (`INST_ID`,`TS`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_server`;
CREATE TABLE `t_server` (
  `SERVER_IP`    varchar(16)   NOT NULL COMMENT '服务器IP',
  `SERVER_NAME`  varchar(32)   COMMENT '服务器主机名',
  PRIMARY KEY (`SERVER_IP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_ssh`;
CREATE TABLE `t_ssh` (
  `SSH_NAME`     varchar(32)   NOT NULL COMMENT 'SSH用户名',
  `SSH_PWD`      varchar(32)   NOT NULL COMMENT 'SSH密码',
  `SERV_TYPE`    varchar(64)   NOT NULL COMMENT '安装的服务类型:MQ,CACHE,DB',
  `SERVER_IP`    varchar(16)   NOT NULL COMMENT '服务器IP',
  UNIQUE KEY `SSH_KEY` (`SSH_NAME`,`SERVER_IP`),
  CONSTRAINT `SSH_IP_FOREIGN` FOREIGN KEY (`SERVER_IP`) REFERENCES `t_server` (`SERVER_IP`) ON DELETE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_mq_queue`;
CREATE TABLE `t_mq_queue` (
  `QUEUE_ID` varchar(36) NOT NULL COMMENT '队列ID UUID',
  `QUEUE_NAME` varchar(48) NOT NULL COMMENT '队列名',
  `IS_DURABLE` varchar(1) DEFAULT '0' COMMENT '0：非持久化；1：持久化',
  `IS_ORDERED` varchar(1) NOT NULL DEFAULT '0' COMMENT '是否全局有序',
  `QUEUE_TYPE` varchar(1) NOT NULL COMMENT '队列类型（1：队列queue；2：广播topic）',
  `IS_DEPLOY` varchar(1) DEFAULT '0' COMMENT '部署标记(0：未部署；1：已部署)',
  `SERV_ID` varchar(36) NOT NULL COMMENT '所属的服务ID',
  `REC_TIME` bigint(14) NOT NULL COMMENT '添加时间',
  PRIMARY KEY (`QUEUE_ID`),
  UNIQUE KEY `IDX_QNAME` (`QUEUE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `t_mq_permnent_topic`;
CREATE TABLE `t_mq_permnent_topic` (
  `CONSUMER_ID` varchar(48) NOT NULL,
  `REAL_QUEUE` varchar(48) DEFAULT NULL,
  `MAIN_TOPIC` varchar(48) DEFAULT NULL,
  `SUB_TOPIC` varchar(48) DEFAULT NULL,
  `QUEUE_ID` varchar(36) NOT NULL,
  PRIMARY KEY (`CONSUMER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `t_metasvr_url`;
CREATE TABLE `t_metasvr_url` (
  `METASVR_ID`   int         NOT NULL COMMENT 'METASERVER ID',
  `METASVR_ADDR` varchar(48) NOT NULL COMMENT 'METASERVER ADDR IP:Port',
  PRIMARY KEY (`METASVR_ID`),
  UNIQUE KEY `IDX_METASVR_ADDR` (`METASVR_ADDR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

INSERT INTO `t_metasvr_url`(`METASVR_ID`, `METASVR_ADDR`) VALUES
(1, '192.168.14.206:19991');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;