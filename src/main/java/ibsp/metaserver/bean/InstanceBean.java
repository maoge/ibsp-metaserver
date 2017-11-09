package ibsp.metaserver.bean;

import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class InstanceBean extends BeanMapper {
	private String instID;      // t_instance.INST_ID
	private int cmptID;         // t_instance.CMPT_ID
	private String isDeployed;  // t_instance.IS_DEPLOYED
	private int x;              // t_instance.POS_X
	private int y;              // t_instance.POS_Y
	private int width;          // t_instance.WIDTH
	private int height;         // t_instance.HEIGHT
	private int row;            // t_instance.ROW
	private int col;            // t_instance.COL
	
	public InstanceBean(String instID, int cmptID, String isDeployed, int x,
			int y, int width, int height, int row, int col) {
		super();
		this.instID = instID;
		this.cmptID = cmptID;
		this.isDeployed = isDeployed;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.row = row;
		this.col = col;
	}

	public String getInstID() {
		return instID;
	}

	public void setInstID(String instID) {
		this.instID = instID;
	}

	public int getCmptID() {
		return cmptID;
	}

	public void setCmptID(int cmptID) {
		this.cmptID = cmptID;
	}
	
	public String getIsDeployed() {
		return isDeployed;
	}

	public void setIsDeployed(String isDeployed) {
		this.isDeployed = isDeployed;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	public boolean isPosDefault() {
		return x == 0 && y == 0 && width == CONSTS.POS_DEFAULT_VALUE
				&& height == CONSTS.POS_DEFAULT_VALUE
				&& row == CONSTS.POS_DEFAULT_VALUE
				&& col == CONSTS.POS_DEFAULT_VALUE;
	}
	
	public JsonObject getPosAsJson() {
		if (isPosDefault())
			return null;
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_X, x);
		json.put(FixHeader.HEADER_Y, y);
		
		if (width != CONSTS.POS_DEFAULT_VALUE
				&& height != CONSTS.POS_DEFAULT_VALUE) {
			json.put(FixHeader.HEADER_WIDTH,  width);
			json.put(FixHeader.HEADER_HEIGHT, height);
		}
		
		if (row != CONSTS.POS_DEFAULT_VALUE 
				&& col != CONSTS.POS_DEFAULT_VALUE) {
			json.put(FixHeader.HEADER_ROW, row);
			json.put(FixHeader.HEADER_COL, col);
		}
		
		return json;
	}
	
	public static InstanceBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		String instID     = getFixDataAsString(mapper, "INST_ID");
		int cmptID        = getFixDataAsInt(mapper, "CMPT_ID");
		String isDeployed = getFixDataAsString(mapper, "IS_DEPLOYED");
		int x             = getFixDataAsInt(mapper, "POS_X");
		int y             = getFixDataAsInt(mapper, "POS_Y");
		int width         = getFixDataAsInt(mapper, "WIDTH");
		int height        = getFixDataAsInt(mapper, "HEIGHT");
		int row           = getFixDataAsInt(mapper, "ROW");
		int col           = getFixDataAsInt(mapper, "COL");
		
		return new InstanceBean(instID, cmptID, isDeployed, x, y, width, height, row, col);
	}

}
