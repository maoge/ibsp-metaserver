package ibsp.metaserver.utils;

public class CONSTS {
	
	public static final String CONF_PATH           = "conf";
	public static final String INIT_PROP_FILE      = "init";
	public static final String LOG4J_CONF          = "log4j";
	public static final String C3P0_PROP_FILE      = "c3p0";
	public static final String HAZELCAST_CONF_FILE = "hazelcast.xml";
	
	public static final String SYS_EVENT_QUEUE     = "sys.event";
	
	public static final int REVOKE_OK              = 0;
	public static final int REVOKE_NOK             = -1;
	public static final int REVOKE_NOK_QUEUE_EXIST = -2;
	public static final int REVOKE_AUTH_FAIL       = -3;
	public static final int REVOKE_AUTH_IP_LIMIT   = -4;
	
	public static final int POS_DEFAULT_VALUE      = -1;
	
	public static final long STAT_COMPTE_INTERVAL  = 1000L;
	public static final long DBPOOL_CHECK_INTERVAL = 6000L;
	
	public static final String HTTP_METHOD_GET     = "GET";
	public static final String HTTP_METHOD_POST    = "POST";
	
	public static final String PATH_SPLIT          = "/";
	public static final String PATH_COMMA          = ",";
	
	public static final String AUTO_GEN_Y          = "1";
	public static final String AUTO_GEN_N          = "0";
	
	public static final int FIX_HEAD_LEN           = 10;
	public static final int FIX_PREHEAD_LEN        = 6;
	public static final byte[] PRE_HEAD            = {'$','H','E','A','D',':'};
	
	public static final String OP_TYPE_ADD         = "1";
	public static final String OP_TYPE_MOD         = "2";
	public static final String OP_TYPE_DEL         = "3";
	
	public static final int TOPO_TYPE_LINK         = 1;
	public static final int TOPO_TYPE_CONTAIN      = 2;
	
	public static final String SERV_TYPE_MQ        = "MQ";
	public static final String SERV_TYPE_CACHE     = "CACHE";
	public static final String SERV_TYPE_DB        = "DB";
	
	public static final String SCHEMA_OBJECT       = "\"object\"";
	public static final String SCHEMA_ARRAY        = "\"array\"";
	
	public static final String ERR_PARAM_INCOMPLETE     = "parameter incomplete ......";
	public static final String ERR_TIDB_CONTAINER_META  = "tidb container component meta error ......";
	public static final String ERR_JSON_SCHEME_VALI_ERR = "json schema validation fail ......";
	public static final String ERR_SCHEMA_FILE_NOT_EXIST= "schema file not exist ......";
	public static final String ERR_METADATA_NOT_FOUND   = "meta data not found ......";
	public static final String ERR_JSONNODE_NOT_COMPLETE= "json node not complete ......";
	public static final String ERR_SERV_TYPE_NOT_FOUND  = "service type not found ......";
	
}
