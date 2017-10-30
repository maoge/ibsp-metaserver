package ibsp.metaserver.bean;

import ibsp.metaserver.utils.CONSTS;

public class PosBean {
	
	private int x;
	private int y;
	private int width;
	private int height;
	private int row;
	private int col;
	
	public PosBean() {
		this.x = 0;
		this.y = 0;
		this.width  = CONSTS.POS_DEFAULT_VALUE;
		this.height = CONSTS.POS_DEFAULT_VALUE;
		this.row = CONSTS.POS_DEFAULT_VALUE;
		this.col = CONSTS.POS_DEFAULT_VALUE;
	}
	
	public PosBean(int x, int y, int width, int height, int row, int col) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.row = row;
		this.col = col;
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

}
