package ibsp.metaserver.utils;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtils {

	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	private static final int READ_TIMEOUT = 3000;
	private static final int CONN_TIMEOUT = 3000;

	private static final String PASSWD_PATTERN_STR = "^(?![a-zA-z\\d]*$)(?![!@#$%^&*_-]+$)[a-zA-Z\\d!@#$%^&*_-]+$";
	private static final Pattern PASSWD_PATTERN;
	
	static {
		PASSWD_PATTERN = Pattern.compile(PASSWD_PATTERN_STR);
	}
	
	public static Map<String, String> getParamForMap(RoutingContext routeContext) {
		return getParamForMap(routeContext.request());
	}

	public static Map<String, String> getParamForMap(HttpServerRequest request) {
		Map<String, String> paramMap = null;
		MultiMap map = null;
		MultiMap map2 = null;
		if (request != null) {
			/*HttpMethod method = request.method();
			if (method.equals(HttpMethod.POST)) {
			} else if (method.equals(HttpMethod.GET)) {
			}*/
			map = request.formAttributes();
			map2 = request.params();

			if (map != null) {
				int i = 0;

				Iterator<Entry<String, String>> it = map.iterator();
				while (it.hasNext()) {
					if (i == 0) {
						paramMap = new HashMap<String, String>();
						i++;
					}

					Entry<String, String> e = it.next();
					paramMap.put(e.getKey(), e.getValue());
				}
			}
			if (map2 != null) {
				int i = 0;

				Iterator<Entry<String, String>> it = map2.iterator();
				while (it.hasNext()) {
					if (i == 0) {
						paramMap = new HashMap<String, String>();
						i++;
					}

					Entry<String, String> e = it.next();
					paramMap.put(e.getKey(), e.getValue());
				}
			}
		}

		return paramMap;
	}

	public static void outJsonObject(RoutingContext routeContext, JsonObject json) {
		outJsonObject(routeContext.response(), json);
	}

	public static void outJsonObject(HttpServerResponse response, JsonObject json) {
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Content-type", "application/json; charset=UTF-8");
		
		response.end(json != null ? json.toString() : "{}");
	}

	public static void outJsonArray(RoutingContext routeContext, JsonArray jsonArray) {
		MultiMap headers = routeContext.request().headers();
		boolean isOrigin = headers.get("Origin") != null;
		outJsonArray(routeContext.response(), jsonArray, isOrigin);
	}

	public static void outJsonArray(HttpServerResponse response, JsonArray jsonArray, boolean isOrigin) {
		if (isOrigin)
			response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Content-type", "application/json; charset=UTF-8");
		if (jsonArray != null) {
			response.end(jsonArray != null ? jsonArray.toString() : "[]");
		} else
			response.end();
	}

	public static void writeRetBuffer(HttpServerResponse response, int retCode, String retInfo) {
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Content-type", "application/json; charset=UTF-8");
		String sRet = getRetInfo(retCode, retInfo);
		response.end(sRet);
	}

	public static void outMessage(RoutingContext routeContext, String str) {
		outMessage(routeContext.response(), str);
	}

	public static void outMessage(HttpServerResponse response, String str) {
		String conTentType = "application/json; charset=UTF-8";
		if (!isJson(str)) {
			conTentType = "text/html; charset=UTF-8";
		}
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Content-type", conTentType);
		if (HttpUtils.isNotNull(str))
			response.end(str);
		else
			response.end();
	}

	private static String getRetInfo(int retCode, String retInfo) {
		JsonObject jsonObj = new JsonObject();

		jsonObj.put(FixHeader.HEADER_RET_CODE, retCode);
		jsonObj.put(FixHeader.HEADER_RET_INFO, retInfo);

		return jsonObj.toString();
	}

	public static boolean isNotNull(Object obj) {
		if (obj == null || "".equals(obj) || "null".equalsIgnoreCase(String.valueOf(obj))) {
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean isNull(Object obj) {
		return (obj == null || "".equals(obj)) ? true : false;
	}

	public static String getUrlData(String urlString, String userName, String userPwd)
			throws IOException, ConnectException {
		String res = "";
		URL url;
		StringBuffer bs = null;
		BufferedReader buffer = null;
		HttpURLConnection urlcon = null;
		
		boolean isConn = false;
		
		try {
			url = new URL(urlString);
			urlcon = (HttpURLConnection) url.openConnection();
			String userPassword = userName + ":" + userPwd;
			urlcon.setRequestProperty("Authorization", "Basic " + HttpUtils.base64(userPassword));
			
			urlcon.setRequestMethod(CONSTS.HTTP_METHOD_GET);
			urlcon.setDoOutput(false);
			urlcon.setReadTimeout(READ_TIMEOUT);
			urlcon.setConnectTimeout(CONN_TIMEOUT);
			
			isConn = true;
			
			buffer = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
			bs = new StringBuffer();
			String s = null;
			while ((s = buffer.readLine()) != null) {
				bs.append(s);
			}
			res = bs.toString();
		} catch (IOException e) {
			throw new IOException(urlString + "\r url调用异常", e);
		} finally {
			if (buffer != null)
				try {
					buffer.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			if (urlcon != null && isConn) {
				urlcon.disconnect();
			}
		}
		return res;
	}

	public static Map<String, Object> jsonStrToMap(String str) {
		return jsonObjectToMap(strToJSON(str));
	}

	public static JsonObject strToJSON(String str) {
		JsonObject json = null;
		if (isNotNull(str) && !str.equals("[]")) {
			json = new JsonObject(str);
		}
		return json;
	}

	public static Map<String, Object> jsonObjectToMap(JsonObject jsonObj) {
		Map<String, Object> map = null;
		if (jsonObj != null) {
			Iterator<Entry<String, Object>> it = jsonObj.iterator();
			map = new HashMap<String, Object>();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

	public static String base64(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	public static boolean isJson(String value) {
		boolean res = false;
		if (isNotNull(value)) {
			String sign = value.substring(0, 1) + value.substring(value.length() - 1);
			if (sign.equals("{}") || sign.equals("[]")) {
				res = true;
			}
		}
		return res;
	}

	public static String getCurrMonth() {
		DecimalFormat df = new DecimalFormat("00");
		Calendar cal = Calendar.getInstance();
		String res = df.format(cal.get(Calendar.MONTH) + 1);
		return res;
	}

	public static String objToStr(Object obj) {
		if (null == obj || obj.equals("null") || String.valueOf(obj).startsWith("-") || obj.equals("")
				|| obj.equals("0.0"))
			return "0";
		else
			return String.valueOf(obj);
	}

	public static String strFormat(Object obj) {
		return numFormat(Double.parseDouble(objToStr(obj)));
	}

	public static String strFormat(Object obj, int k) {
		return numFormat(Double.parseDouble(objToStr(obj)) / k);
	}

	public static double strFormatForDouble(Object obj) {
		return Double.parseDouble(objToStr(obj));
	}

	public static String numFormat(double s) {
		DecimalFormat df = new DecimalFormat();
		df.applyPattern("#");
		return df.format(s);
	}

	public static long getCurrTimestamp() {
		return System.currentTimeMillis();
	}

	public static Map<String, Object> convertBean(Object bean)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		Class<?> type = bean.getClass();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		BeanInfo beanInfo = Introspector.getBeanInfo(type);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			String propertyName = descriptor.getName();
			if (!propertyName.equals("class")) {
				Method readMethod = descriptor.getReadMethod();
				Object result = readMethod.invoke(bean, new Object[0]);
				if (result != null) {
					returnMap.put(propertyName, result);
				} else {
					returnMap.put(propertyName, "");
				}
			}
		}
		return returnMap;
	}
	
	public static String getMonthByTimestamp(String simestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(Long.parseLong(simestamp)));
		int month = cal.get(Calendar.MONTH);
		
		return String.format("%02d", month + 1);
	}
	
	public static String concateArray(String[] arr) {
		if (arr == null || arr.length == 0)
			return "";
		
		int i = arr.length - 1;
		StringBuilder sb = new StringBuilder();
		for (; i >= 0; i--) {
			sb.append(arr[i]);
			if (i > 0)
				sb.append(CONSTS.PATH_COMMA);
		}
		
		return sb.toString();
	}
	
	public static boolean chkPasswdComplexity(String passwd) {
		boolean ret = false;
		try {
			Matcher match = PASSWD_PATTERN.matcher(passwd);
			ret = match.find();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return ret;
	}
}
