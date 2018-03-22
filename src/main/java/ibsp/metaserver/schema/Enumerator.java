package ibsp.metaserver.schema;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

public class Enumerator {
	
	private static final String FIELD_PROPERTIES = "properties";
	private static final String FIELD_ITEMS = "items";
	private static final String FIELD_POS = "POS";
	private static final String TYPE = "type";
	private static final String STRING = "string";
	
	public static void enumSchema(JsonNode node, Map<String, String> skeleton) {
		if (node == null)
			return;
		
		JsonNode propNode = node.get(FIELD_PROPERTIES);
		if (propNode == null) {
			propNode = node.get(FIELD_ITEMS);
			if (propNode != null) {
				propNode = propNode.get(FIELD_PROPERTIES);
			} else {
				return;
			}
		}
		
		Iterator<Entry<String, JsonNode>> itFields = propNode.fields();
		while (itFields.hasNext()) {
			Map.Entry<String,JsonNode> entry = itFields.next();
			
			JsonNode elemNode = entry.getValue();
			String nodeName = entry.getKey();
			JsonNode elemType = elemNode.get(TYPE);
			if (elemType == null)
				continue;
			
			if (elemType.asText().equals(STRING)) {
				continue;
			}
			
			if (nodeName.equals(FIELD_POS)) {
				continue;
			}
			
			skeleton.put(nodeName, elemNode.get("type").toString());
			enumSchema(elemNode, skeleton);
		}
	}

}
