package ibsp.metaserver.bean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SqlBean {

	private String sql;
	private List<Object> params;

	public SqlBean() {
	}
	
	public SqlBean(String sql, List<Object> params) {
		this.sql = sql;
		this.params = params;
	}

	public SqlBean(String sql) {
		this.sql = sql;
	}

	public void putParam(Object param) {
		if(params==null)
			params = new LinkedList<Object>();
		params.add(param);
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}
	
	public void addParams(Object[] obj){
		this.params = Arrays.asList(obj);
	}

	public List<Object> getParams() {
		return params;
	}

}
