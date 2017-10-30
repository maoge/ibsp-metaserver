package ibsp.metaserver.utils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
//import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public class JsonSchemaValidator {
	
	private static Logger logger = LoggerFactory.getLogger(JsonSchemaValidator.class);
	
	private static final JsonValidator VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
	private static Map<String, JsonNode> SCHEMA_RES_MAP = null;
	
	static {
		SCHEMA_RES_MAP = new ConcurrentHashMap<String, JsonNode>();
	}
	
	private static ProcessingReport validator(String schemaRes, String name,
			String instance) throws IOException, ProcessingException {
		
		JsonNode schema = SCHEMA_RES_MAP.get(name);
		if (schema == null) {
			schema = JsonLoader.fromResource(schemaRes);
			SCHEMA_RES_MAP.put(name, schema);
		}
		
		JsonNode instanceNode = JsonLoader.fromString(instance);
		ProcessingReport report = VALIDATOR.validate(schema, instanceNode);
		
		return report;
	}
	
	public static boolean validateTiDBJson(String sTiDBJson) throws IOException, ProcessingException {
		ProcessingReport report = JsonSchemaValidator.validator("/schema/tidb.schema", "tidb", sTiDBJson);
		if (!report.isSuccess()) {
			logger.error(report.toString());
		}
		return report.isSuccess();
	}
	
}
