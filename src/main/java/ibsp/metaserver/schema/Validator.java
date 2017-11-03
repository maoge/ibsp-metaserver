package ibsp.metaserver.schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public class Validator {
	
	private static final JsonValidator VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
	private static Map<String, JsonNode> SCHEMA_RES_MAP = null;
	private static Map<String, Map<String, String>> META_SKELETON = null;
	
	static {
		SCHEMA_RES_MAP = new ConcurrentHashMap<String, JsonNode>();
		META_SKELETON = new ConcurrentHashMap<String, Map<String, String>>();
	}
	
	public static JsonNode getSchema(String name) throws IOException {
		JsonNode schema = SCHEMA_RES_MAP.get(name);
		if (schema == null) {
			String path = String.format("/schema/%s.schema", name);
			schema = JsonLoader.fromResource(path);

			if (schema != null) {
				Map<String, String> skeleton = new HashMap<String, String>();
				Enumerator.enumSchema(schema, skeleton);

				SCHEMA_RES_MAP.put(name, schema);
				META_SKELETON.put(name, skeleton);
			}
		}

		return schema;
	}
	
	public static Map<String, String> getSkeleton(String name)
			throws IOException {
		Map<String, String> skeleton = META_SKELETON.get(name);
		if (skeleton == null) {
			String path = String.format("/schema/%s.schema", name);
			JsonNode schema = JsonLoader.fromResource(path);

			if (schema != null) {
				skeleton = new HashMap<String, String>();
				Enumerator.enumSchema(schema, skeleton);

				SCHEMA_RES_MAP.put(name, schema);
				META_SKELETON.put(name, skeleton);
			}
		}

		return skeleton;
	}
	
	public static ProcessingReport validator(String name, String instance)
			throws IOException, ProcessingException {

		JsonNode schema = getSchema(name);
		JsonNode instanceNode = JsonLoader.fromString(instance);
		ProcessingReport report = VALIDATOR.validate(schema, instanceNode);

		return report;
	}
	
}
