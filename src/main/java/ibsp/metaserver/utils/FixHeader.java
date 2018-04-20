package ibsp.metaserver.utils;

public class FixHeader {
	
	public static final String HEADER_UUID                     = "UUID";
	public static final String HEADER_ID                       = "ID";
	public static final String HEADER_NAME                     = "NAME";
	public static final String HEADER_OP_TYPE                  = "OP_TYPE";
	public static final String HEADER_SESSION_KEY              = "SESSION_KEY";
	
	public static final String HEADER_TS                       = "TS";
	public static final String HEADER_CPU                      = "CPU";
	public static final String HEADER_MEM                      = "MEM";
	public static final String HEADER_DISK                     = "DISK";
	public static final String HEADER_USED                     = "Used";
	public static final String HEADER_TOTAL                    = "Total";
	public static final String HEADER_AVAILABLE                = "Available";
	
	public static final String HEADER_QUOTA_CODE               = "QUOTA_CODE";
	public static final String HEADER_QUOTA_MEAN               = "QUOTA_MEAN";
	public static final String HEADER_START_TS                 = "START_TS";
	public static final String HEADER_END_TS                   = "END_TS";
	
	public static final String HEADER_SERVICE_ID               = "SERV_ID";
	public static final String HEADER_SERVICE_NAME             = "SERV_NAME";
	
	public static final String HEADER_VBROKER_ID               = "VBROKER_ID";
	public static final String HEADER_VBROKER_IDS              = "VBROKER_IDS";
	public static final String HEADER_VBROKER_NAME             = "VBROKER_NAME";
	public static final String HEADER_BROKER_ID                = "BROKER_ID";
	public static final String HEADER_BROKER_NAME              = "BROKER_NAME";
	public static final String HEADER_BROKER_INFO              = "BROKER_INFO";
	public static final String HEADER_HOSTNAME                 = "HOSTNAME";
	public static final String HEADER_INSTANCE_NAME            = "INSTANCE_NAME";
	public static final String HEADER_IP                       = "IP";
	public static final String HEADER_MGR_PORT                 = "MGR_PORT";
	public static final String HEADER_MQ_PWD                   = "MQ_PWD";
	public static final String HEADER_MQ_USER                  = "MQ_USER";
	public static final String HEADER_OS_PWD                   = "OS_PWD";
	public static final String HEADER_OS_USER                  = "OS_USER";
	public static final String HEADER_PORT                     = "PORT";
	public static final String HEADER_STAT_PORT                = "STAT_PORT";
	public static final String HEADER_REC_TIME                 = "REC_TIME";
	public static final String HEADER_SSH_PORT                 = "SSH_PORT";
	public static final String HEADER_VHOST                    = "VHOST";
	public static final String HEADER_VIP                      = "VIP";
	public static final String HEADER_IS_CLUSTER               = "IS_CLUSTER";
	public static final String HEADER_IS_WRITABLE              = "IS_WRITABLE";
	public static final String HEADER_ROOT_PWD                 = "ROOT_PWD";
	public static final String HEADER_IS_RUNNING               = "IS_RUNNING";
	
	public static final String HEADER_QUEUE_ID                 = "QUEUE_ID";
	public static final String HEADER_QUEUE_NAME               = "QUEUE_NAME";
	public static final String HEADER_IS_DURABLE               = "IS_DURABLE";
	public static final String HEADER_GLOBAL_ORDERED           = "IS_ORDERED";
	public static final String HEADER_QUEUE_TYPE               = "QUEUE_TYPE";
	public static final String HEADER_IS_DEPLOY                = "IS_DEPLOY";
	public static final String HEADER_ERL_COOKIE               = "ERL_COOKIE";
	
	public static final String HEADER_MASTER_ID                = "MASTER_ID";
	public static final String HEADER_SLAVE_ID                 = "SLAVE_ID";
	
	public static final String HEADER_MAIN_TOPIC               = "MAIN_TOPIC";
	public static final String HEADER_SUB_TOPIC                = "SUB_TOPIC";
	
	public static final String HEADER_GROUP_ID                 = "GROUP_ID";
	public static final String HEADER_GROUP_IDS                = "GROUP_IDS";
	public static final String HEADER_GROUP_NAME               = "GROUP_NAME";
	public static final String HEADER_GROUP_VBROKER_THRESHHOLD = "VBROKER_THRESHHOLD";

	
	public static final String HEADER_MSG_CNT                  = "MSG_CNT";
	
	public static final String HEADER_RET_CODE                 = "RET_CODE";
	public static final String HEADER_RET_INFO                 = "RET_INFO";
	
	public static final String HEADER_EVENT_CODE               = "EVENT_CODE";
	
	public static final String HEADER_PRODUCE_RATE             = "PRODUCE_RATE";
	public static final String HEADER_CONSUME_RATE             = "CONSUME_RATE";
	
	public static final String HEADER_DURABLE                  = "DURABLE";
	public static final String HEADER_AUTODELETE               = "AUTODELETE";
	public static final String HEADER_ARGUMENTS                = "ARGUMENTS";
	public static final String HEADER_NODE_NAME                = "NODE_NAME";
	public static final String HEADER_MEMARY                   = "MEMARY";
	public static final String HEADER_PRODUCE_COUNTS           = "PRODUCE_COUNTS";
	public static final String HEADER_CONSUME_COUNTS           = "CONSUME_COUNTS";
	
	public static final String HEADER_VIP_PORT                 = "VIP_PORT";
	public static final String HEADER_CLUSTER_NAME             = "CLUSTER_NAME";
	public static final String HEADER_CONSUMERS                = "CONSUMERS";
	public static final String HEADER_QUEUES                   = "QUEUES";
	public static final String HEADER_CONNECTIONS              = "CONNECTIONS";
	public static final String HEADER_CONNECTIONS_RATE         = "CONNECTIONS_RATE";
	public static final String HEADER_NODEINFO_JSONSTR         = "NODEINFO_JSONSTR";
	
	public static final String HEADER_ALARM_CODE               = "ALARM_CODE";
	public static final String HEADER_ALARM_DESC               = "ALARM_DESC";
	
	public static final String HEADER_CLIENT_INFO              = "CLIENT_INFO";
	public static final String HEADER_LSNR_ADDR                = "LSNR_ADDR";
	public static final String HEADER_CLIENT_TYPE              = "CLIENT_TYPE";
	public static final String HEADER_CLNT_IP_AND_PORT         = "CLIENT_IP_AND_PORT";
	public static final String HEADER_BKR_IP_AND_PORT          = "BROKER_IP_AND_PORT";
	public static final String HEADER_CLIENT_PRO_TPS           = "CLIENT_PRO_TPS";
	public static final String HEADER_CLIENT_CON_TPS           = "CLIENT_CON_TPS";
	public static final String HEADER_T_PRO_MSG_COUNT          = "TOTAL_PRO_MSG_COUNT";
	public static final String HEADER_T_PRO_MSG_BYTES          = "TOTAL_PRO_MSG_BYTES";
	public static final String HEADER_T_CON_MSG_COUNT          = "TOTAL_CON_MSG_COUNT";
	public static final String HEADER_T_CON_MSG_BYTES          = "TOTAL_CON_MSG_BYTES";
	
	public static final String HEADER_LAST_TIMESTAMP           = "LAST_TIMESTAMP";
	public static final String HEADER_CLIENT_INFOS             = "CLIENT_INFOS";
	
	public static final String HEADER_BIND_TYPE                = "BIND_TYPE";
	public static final String HEADER_CONSUMER_ID              = "CONSUMER_ID";
	public static final String HEADER_SRC_QUEUE                = "SRC_QUEUE";
	public static final String HEADER_MAIN_KEY                 = "MAINKEY";
	public static final String HEADER_SUB_KEY                  = "SUBKEY";
	public static final String HEADER_REAL_QUEUE               = "REAL_QUEUE";
	public static final String HEADER_PERM_QUEUE               = "PERM_QUEUE";
	
	public static final String HEADER_REC_ID                   = "REC_ID";
	public static final String HEADER_USER_ID                  = "USER_ID";
	public static final String HEADER_USER_NAME                = "USER_NAME";
	public static final String HEADER_LOGIN_PWD                = "LOGIN_PWD";
	public static final String HEADER_USER_PWD                 = "USER_PWD";
	public static final String HEADER_USER_STATUS              = "USER_STATUS";
	public static final String HEADER_LINE_STATUS              = "LINE_STATUS";
	public static final String HEADER_REC_STATUS               = "REC_STATUS";
	public static final String HEADER_REC_PERSON               = "REC_PERSON";
	
	public static final String HEADER_ROLE_ID                  = "ROLE_ID";
	public static final String HEADER_ROLE_IDS                 = "ROLE_IDS";
	public static final String HEADER_ROLE_NAME                = "ROLE_NAME";
	public static final String HEADER_PARENT_ID                = "PARENT_ID";
	public static final String HEADER_ROLE_DEC                 = "ROLE_DEC";	
	
	public static final String HEADER_ACTIVE_COLL_INFO         = "ACTIVE_COLL_INFO";
	
	public static final String HEADER_API_TYPE                 = "API_TYPE";
	public static final String HEADER_JSONSTR                  = "JSON_STR";
	
	public static final String HEADER_LOCAL_IP                 = "LOCAL_IP";
	public static final String HEADER_LOCAL_PORT               = "LOCAL_PORT";
	public static final String HEADER_REMOTE_IP                = "REMOTE_IP";
	public static final String HEADER_REMOTE_PORT              = "REMOTE_PORT";
	
	public static final String HEADER_MSG_READY                = "messages_ready";
	public static final String HEADER_MSG_UNACK                = "messages_unacknowledged";
	public static final String HEADER_QUEEU_TOTAL_MSG          = "queue_totals.messages";
	public static final String HEADER_MSG_TOTAL                = "msg_total";
	public static final String HEADER_QUEUE_TOTALS             = "queue_totals";
	public static final String HEADER_MESSAGES                 = "messages";
	
	public static final String HEADER_KEY                      = "key";
	public static final String HEADER_TIMESTAMP                = "TIMESTAMP";
	public static final String HEADER_POS                      = "POS";
	public static final String HEADER_X                        = "x";
	public static final String HEADER_Y                        = "y";
	public static final String HEADER_WIDTH                    = "width";
	public static final String HEADER_HEIGHT                   = "height";
	public static final String HEADER_ROW                      = "row";
	public static final String HEADER_COL                      = "col";
	
	public static final String HEADER_LOGIN_TIME               = "LOGIN_TIME";
	public static final String HEADER_MAGIC_KEY                = "MAGIC_KEY";
	public static final String HEADER_IS_ADMIN                 = "IS_ADMIN";
	
	public static final String HEADER_BLACK_WHITE_LIST_IP      = "IP";
	public static final String HEADER_BLACK_WHITE_REMARKS      = "REMARKS";
	public static final String HEADER_BLACK_WHITE_TYPE         = "TYPE";
	
	public static final String HEADER_ATTR_ID                  = "ATTR_ID";
	public static final String HEADER_ATTR_NAME                = "ATTR_NAME";
	public static final String HEADER_ATTR_NAME_CN             = "ATTR_NAME_CN";
	public static final String HEADER_AUTO_GEN                 = "AUTO_GEN";
	
	public static final String HEADER_INSTANCE_ID              = "INST_ID";
	public static final String HEADER_INSTANCE_ADDRESS         = "INST_ADD";
	public static final String HEADER_CMPT_TYPE                = "CMPT_TYPE";
	public static final String HEADER_CMPT_ID                  = "CMPT_ID";
	public static final String HEADER_CMPT_NAME                = "CMPT_NAME";
	public static final String HEADER_CMPT_NAME_CN             = "CMPT_NAME_CN";
	public static final String HEADER_IS_NEED_DEPLOY           = "IS_NEED_DEPLOY";
	public static final String HEADER_SERV_CLAZZ               = "SERV_CLAZZ";
	public static final String HEADER_SERV_TYPE                = "SERV_TYPE";
	public static final String HEADER_SERV_ID                  = "SERV_ID";
	public static final String HEADER_SERV_NAME                = "SERV_NAME";
	public static final String HEADER_SUB_SERV_TYPE            = "SUB_SERV_TYPE";
	
	public static final String HEADER_TIDB_JSON                = "TIDB_JSON";
	public static final String HEADER_TOPO_JSON                = "TOPO_JSON";
	public static final String HEADER_NODE_JSON                = "NODE_JSON";
	public static final String HEADER_DB_SERV_CONTAINER        = "DB_SERV_CONTAINER";
	public static final String HEADER_DB_SVC_CONTAINER_ID      = "DB_SVC_CONTAINER_ID";
	public static final String HEADER_DB_SVC_CONTAINER_NAME    = "DB_SVC_CONTAINER_NAME";
	
	public static final String HEADER_DB_TIDB_CONTAINER        = "DB_TIDB_CONTAINER";
	public static final String HEADER_TIDB_CONTAINER_ID        = "TIDB_CONTAINER_ID";
	public static final String HEADER_TIDB_CONTAINER_NAME      = "TIDB_CONTAINER_NAME";
	public static final String HEADER_DB_TIDB                  = "DB_TIDB";
	public static final String HEADER_TIDB_ID                  = "TIDB_ID";
	public static final String HEADER_TIDB_NAME                = "TIDB_NAME";
	
	public static final String HEADER_DB_TIKV_CONTAINER        = "DB_TIKV_CONTAINER";
	public static final String HEADER_TIKV_CONTAINER_ID        = "TIKV_CONTAINER_ID";
	public static final String HEADER_TIKV_CONTAINER_NAME      = "TIKV_CONTAINER_NAME";
	public static final String HEADER_DB_TIKV                  = "DB_TIKV";
	public static final String HEADER_TIKV_ID                  = "TIKV_ID";
	public static final String HEADER_TIKV_NAME                = "TIKV_NAME";
	
	public static final String HEADER_DB_PD_CONTAINER          = "DB_PD_CONTAINER";
	public static final String HEADER_PD_CONTAINER_ID          = "PD_CONTAINER_ID";
	public static final String HEADER_PD_CONTAINER_NAME        = "PD_CONTAINER_NAME";
	public static final String HEADER_DB_PD                    = "DB_PD";
	public static final String HEADER_PD_ID                    = "PD_ID";
	public static final String HEADER_PD_NAME                  = "PD_NAME";
	
	public static final String HEADER_DB_COLLECTD              = "DB_COLLECTD";
	public static final String HEADER_COLLECTD_ID              = "COLLECTD_ID";
	public static final String HEADER_COLLECTD_NAME            = "COLLECTD_NAME";
	
	public static final String HEADER_ATTRIBUTES               = "ATTRS";
	public static final String HEADER_DEPLOY_FLAG              = "DEPLOY_FLAG";
	
	public static final String HEADER_SQL_STR                  = "SQL_STR";
	public static final String HEADER_SCHEMA_NAME              = "SCHEMA_NAME";
	
	public static final String HEADER_PAGE_SIZE                = "pageSize";
	public static final String HEADER_PAGE_NUMBER              = "pageNumber";
	
	public static final String HEADER_CLUSTER_ID               = "CLUSTER_ID";
	public static final String HEADER_NEW_MASTER_ID            = "NEW_MASTER_ID";
}
