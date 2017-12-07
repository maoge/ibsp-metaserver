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
	
	public static final int REVOKE_OK                = 0;
	public static final int REVOKE_NOK               = -1;
	public static final int REVOKE_NOK_QUEUE_EXIST   = -2;
	public static final int REVOKE_AUTH_FAIL         = -3;
	public static final int REVOKE_AUTH_IP_LIMIT     = -4;
	
	public static final int POS_DEFAULT_VALUE        = -1;
	
	public static final long STAT_COMPTE_INTERVAL    = 1000L;
	public static final long DBPOOL_CHECK_INTERVAL   = 6000L;
	public static final long DEPLOY_CHECK_INTERVAL   = 500L;
	
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
	
	public static final String SERV_TYPE_MQ          = "MQ";
	public static final String SERV_TYPE_CACHE       = "CACHE";
	public static final String SERV_TYPE_DB          = "DB";
	
	public static final String SERV_DB_PD            = "DB_PD";
	public static final String SERV_DB_TIDB          = "DB_TIDB";
	public static final String SERV_DB_TIKV          = "DB_TIKV";
	public static final String SERV_DB_COLLECTD      = "DB_COLLECTD";
	
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
	
	//TIDB consts
	public static final String ERR_FIND_TIDB_SERVER_ERROR    = "no available tidb server for serv_id ";
	public static final String ERR_CONNECT_TIDB_SERVER_ERROR = "connect to tidb server failed ......";
	public static final String ERR_NO_PD_TO_JOIN             = "no available pd server to join ......";
}
