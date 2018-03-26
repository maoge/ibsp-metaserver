package ibsp.metaserver.utils;

public class CONSTS {
	
	public static final String CONF_PATH             = "conf";
	public static final String INIT_PROP_FILE        = "conf/init";
	public static final String LOG4J_CONF            = "conf/log4j";
	public static final String C3P0_PROP_FILE        = "conf/c3p0";
	public static final String HAZELCAST_CONF_FILE   = "conf/hazelcast.xml";
	
	public static final String SYS_EVENT_QUEUE       = "sys.event";
	
	public static final long SSH_CMD_TIMEOUT         = 15000;
	public static final String SSH_TIMEOUT_INFO      = "exec remote ssh cmd timeout!";
	
	public static final String COLLECT_DATA_API      = "getCollectData";
	
	public static final String LINE_SEP              = "\n";
	public static final String HOSTS_FILE            = "/etc/hosts";
	public static final String BASH_PROFILE          = ".bash_profile";
	public static final String NO_SUCH_FILE          = "No such file or directory";
	public static final String COMMAND_NOT_FOUND     = "command not found";
	public static final String ERR_COMMAND_NOT_FOUND = "command '%s' not found";
	public static final String FILE_DIR_NOT_EXISTS   = "No such file or directory";
	public static final String NO_MAPPING_IN_HOSTS   = "gethostbyname error!";
	public static final String START_SHELL           = "start.sh";
	public static final String STOP_SHELL            = "stop.sh";
	public static final String SHELL_MACRO           = "#!/bin/sh";
	public static final String SQUARE_BRACKET_LEFT   = "[";
	public static final String SQUARE_BRACKET_RIGHT  = "]";
	
	public static final String DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE = "<span style='color:blue;'>";
	public static final String DEPLOY_SINGLE_FAIL_BEGIN_STYLE    = "<span style='color:yellow;'>";
	public static final String END_STYLE                         = "</span>";
	
	public static final String MQ_DEPLOY_ROOT_PATH     = "mq_deploy";
	public static final String MQ_DEPLOY_PATH          = "rabbitmq_server-3.4.3";
	public static final long DISK_FREE_LIMIT           = 6000000000L;
	public static final float VM_MEMORY_HIGH_WATERMARK = 0.2f;
	
	public static final int REVOKE_OK                = 0;
	public static final int REVOKE_NOK               = -1;
	public static final int REVOKE_NOK_QUEUE_EXIST   = -2;
	public static final int REVOKE_AUTH_FAIL         = -3;
	public static final int REVOKE_AUTH_IP_LIMIT     = -4;
	public static final int SERVICE_NOT_INIT         = -5;
	
	public static final int POS_DEFAULT_VALUE        = -1;
	
	public static final long STAT_COMPTE_INTERVAL        = 1000L;
	public static final long DBPOOL_CHECK_INTERVAL       = 6000L;
	public static final long DEPLOY_CHECK_INTERVAL       = 500L;
	public static final long TIKV_STATE_CHECK_INTERVAL   = 500L;
	
	public static final String HTTP_METHOD_GET       = "GET";
	public static final String HTTP_METHOD_POST      = "POST";
	
	public static final String LINE_END              = "\n";
	public static final String HTML_LINE_END         = "<br/>";
	
	public static final String PATH_SPLIT            = "/";
	public static final String PATH_COMMA            = ",";
	
	public static final String AUTO_GEN_Y            = "1";
	public static final String AUTO_GEN_N            = "0";
	
	public static final int SSH_PORT_DEFAULT         = 22;
	public static final int FIX_HEAD_LEN             = 10;
	public static final int FIX_PREHEAD_LEN          = 6;
	public static final byte[] PRE_HEAD              = {'$','H','E','A','D',':'};
	
	public static final String OP_TYPE_ADD           = "1";
	public static final String OP_TYPE_MOD           = "2";
	public static final String OP_TYPE_DEL           = "3";
	
	public static final String NOT_DEPLOYED          = "0";
	public static final String DEPLOYED              = "1";
	
	public static final String NOT_NEED_DEPLOY       = "0";
	public static final String NEED_DEPLOY           = "1";
	
	public static final int TOPO_TYPE_LINK           = 1;
	public static final int TOPO_TYPE_CONTAIN        = 2;
	
	public static final String MQ_DEFAULT_USER       = "mq";
	public static final String MQ_DEFAULT_PWD        = "ibsp_mq@123321";
	public static final String MQ_DEFAULT_VHOST      = "/";
	public static final long   MQ_DEPLOY_MAXTIME     = 60000l;
	
	public static final String SERV_TYPE_MQ          = "MQ";
	public static final String SERV_TYPE_CACHE       = "CACHE";
	public static final String SERV_TYPE_DB          = "DB";
	
	public static final String SERV_DB_PD            = "DB_PD";
	public static final String SERV_DB_TIDB          = "DB_TIDB";
	public static final String SERV_DB_TIKV          = "DB_TIKV";
	public static final String SERV_COLLECTD         = "COLLECTD";
	
	public static final String SERV_MQ_RABBIT        = "MQ_RABBIT";
	public static final String SERV_MQ_ERLANG        = "MQ_ERLANG";
	
	public static final String SCHEMA_OBJECT         = "\"object\"";
	public static final String SCHEMA_ARRAY          = "\"array\"";
	
	public static final String ERR_PARAM_INCOMPLETE      = "parameter incomplete ......";
	public static final String ERR_TIDB_CONTAINER_META   = "tidb container component meta error ......";
	public static final String ERR_JSON_SCHEME_VALI_ERR  = "json schema validation fail ......";
	public static final String ERR_SCHEMA_FILE_NOT_EXIST = "schema file not exist ......";
	public static final String ERR_METADATA_NOT_FOUND    = "meta data not found ......";
	public static final String ERR_JSONNODE_NOT_COMPLETE = "json node not complete ......";
	public static final String ERR_SERV_TYPE_NOT_FOUND   = "service type not found ......";
	public static final String ERR_HOSTINFO_NOT_COMPLETE = "host info not complete ......";
	public static final String ERR_DEPLOY_CONF_MISS      = "deploy file config missing ......";
	public static final String ERR_SSH_CONNECT_FAIL      = "ssh connect fail ......";
	public static final String ERR_EXEC_SHELL_FAIL       = "exec shell fail ......";
	public static final String ERR_DEPLOY_ERL_FAIL       = "deploy erlang fail ......";
	public static final String ERR_SET_HOSTS_FAIL        = "set hosts fail ......";
	public static final String ERR_MQ_PORT_ISUSED        = "MQ port is used ......";
	public static final String ERR_MQ_NO_BROKER_FUND     = "no broker found ......";
	public static final String ERR_DEPLOY_MQ_FAIL        = "deploy mq fail ......";
	public static final String ERR_UNDEPLOY_MQ_FAIL      = "undeploy mq fail ......";
	public static final String ERR_SET_HOST              = "set hosts fail ......";
	public static final String ERR_SET_ERLCOOKIE         = "set erlang cookie fail ......";
	public static final String ERR_RABBITMQ_PORT_USED    = "rabbitmq port is used ......";
	public static final String ERR_RABBITMQ_MGR_PORT_USED = "rabbitmq manage port is used ......";
	public static final String ERR_PORT_USED_IN_DB       = "port is used in db ......";
	public static final String ERR_PWD_INCORRECT         = "login passwd incorrect ......";
	public static final String ERR_PWD_EXPIRE            = "login passwd expired ......";
	public static final String ERR_PUT_SESSION           = "put session fail ......";
	
	//TIDB consts
	public static final String ERR_FIND_TIDB_SERVER_ERROR    = "no available tidb server for serv_id ";
	public static final String ERR_FIND_PD_SERVER_ERROR      = "no available pd server for serv_id ";
	public static final String ERR_CONNECT_TIDB_SERVER_ERROR = "connect to tidb server failed ......";
	public static final String ERR_NO_PD_TO_JOIN             = "no available pd server to join ......";
	
	public static final String PD_API_STORES                 = "/pd/api/v1/stores";
	
	public static final String PD_DELETE_MEMBER_SUCC         = "Success!";
	public static final String PD_DELETE_STORE_SUCC          = "Success!";
	public static final String TIKV_OFFLINE_STATUS           = "Offline";
	public static final String TIKV_TOMBSTONE_STATUS         = "Tombstone";
	
	public static final int MIN_PD_NUMBER                    = 3;
	public static final int MIN_TIKV_NUMBER                  = 3;
	public static final int MIN_TIDB_NUMBER                  = 2;
}
