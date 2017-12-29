package ibsp.metaserver.bean;

import io.vertx.core.json.JsonObject;

public abstract class CollectDataParser {
	
	protected JsonObject jsonObj;
	
	public CollectDataParser(JsonObject jsonObj) {
		this.jsonObj = jsonObj;
	}
	
	public abstract void parseAndSave();

}
