package json.schema.enumtest;

import ibsp.metaserver.utils.CONSTS;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.github.fge.jackson.JsonLoader;

public class JsonSchemaEnumTest {
	
	private static void EnumSchema(JsonNode node) {
		JsonNode propNode = node.get("properties");
		if (propNode != null) {
			Iterator<Entry<String, JsonNode>> itFields = propNode.fields();
			while (itFields.hasNext()) {
				Map.Entry<String,JsonNode> entry = itFields.next();
				
				JsonNode elemNode = entry.getValue();
				String nodeName = entry.getKey();
				
				if (elemNode.get("type").asText().equals("string")) {
					continue;
				}
				
				if (nodeName.equals("POS")) {
					continue;
				}
				
				String info = String.format("%s:%s", nodeName, elemNode.get("type").toString());
				System.out.println(info);
				
				EnumSchema(elemNode);
			}
		} else {			
			return;
		}
	}
	
	private static String getJsonType(JsonNodeType nodeType) {
		switch (nodeType) {
		case ARRAY:
			return "ARRAY";
		case BINARY:
			return "BINARY";
		case BOOLEAN:
			return "BOOLEAN";
		case MISSING:
			return "MISSING";
		case NULL:
			return "NULL";
		case NUMBER:
			return "NUMBER";
		case OBJECT:
			return "OBJECT";
		case POJO:
			return "POJO";
		case STRING:
			return "STRING";
		default:
			return "";
		}
	}

	public static void main(String[] args) throws IOException {
		JsonNode schemaNode = JsonLoader.fromResource("/schema/tidb.schema");
		EnumSchema(schemaNode);
	}

}
