package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.MetaComponentBean;
import ibsp.metaserver.bean.RelationBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.utils.CRUD;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaDataService {
	
	private static Logger logger = LoggerFactory.getLogger(MetaDataService.class);
	
	public static boolean testDB() {
		String sql = "select 1 from dual";
		boolean ret = false;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				ret = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return ret;
	}
	
	public static List<MetaAttributeBean> getAllMetaAttribute() {
		String sql = "select ATTR_ID, ATTR_NAME, ATTR_NAME_CN, AUTO_GEN from t_meta_attr";
		List<MetaAttributeBean> metaAttrList = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				metaAttrList = new LinkedList<MetaAttributeBean>();
				
				for (HashMap<String, Object> item : resultList) {
					MetaAttributeBean metaAttr = MetaAttributeBean.convert(item);
					metaAttrList.add(metaAttr);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return metaAttrList;
	}
	
	public static List<MetaComponentBean> getAllMetaComponent() {
		String sql = "select CMPT_ID,CMPT_NAME,CMPT_NAME_CN,SERV_CLAZZ,SERV_TYPE,SUB_SERV_TYPE from t_meta_cmpt";
		List<MetaComponentBean> metaCmptList = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				metaCmptList = new LinkedList<MetaComponentBean>();
				
				for (HashMap<String, Object> item : resultList) {
					MetaComponentBean metaCmpt = MetaComponentBean.convert(item);
					metaCmptList.add(metaCmpt);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return metaCmptList;
	}
	
	public static List<RelationBean> getAllCmpt2Attr() {
		String sql = "select CMPT_ID as MASTER_ID, ATTR_ID as SLAVE_ID from t_meta_cmpt_attr";
		List<RelationBean> cmpt2AttrList = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				cmpt2AttrList = new LinkedList<RelationBean>();
				
				for (HashMap<String, Object> item : resultList) {
					RelationBean bean = RelationBean.convert(item);
					cmpt2AttrList.add(bean);
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return cmpt2AttrList;
	}
	
}
