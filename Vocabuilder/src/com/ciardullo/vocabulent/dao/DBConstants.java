package com.ciardullo.vocabulent.dao;

public interface DBConstants {
	public static String DATABASE_PATH = "/data/data/{package}/databases/";
	public static final String DATABASE_NAME = "vocabulent.db";

	public static final String TBL_NM_ALBUM = "album";
	public static final String TBL_NM_ALBUMDESC = "albumdesc";
	public static final String TBL_NM_SCENE = "scene";
	public static final String TBL_NM_SCENEDESC = "scenedesc";
	public static final String TBL_NM_HOTZONE = "hotzone";
	public static final String TBL_NM_ANSWERKEY= "answerkey";
	public static final String TBL_NM_SCOREBOARD = "scoreboard";
	
	public static final String TBL_NM_PURCHASE_HISTORY = "iab_hist";
	public static final String TBL_NM_PURCHASE = "iab";

	public static final int DATABASE_VERSION = 1;
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LKUP_NAME = "lkup_name";
	
	// Columns in hotzone table
	public static final String COLUMN_SCENE_ID = "scene_id";
	public static final String COLUMN_HOT_ID = "hot_id";
	public static final String COLUMN_HOT_DESC = "hot_desc";
	public static final String COLUMN_POINT_LEFT = "point_left";
	public static final String COLUMN_POINT_TOP = "point_top";
	public static final String COLUMN_POINT_RIGHT = "point_right";
	public static final String COLUMN_POINT_BOTTOM = "point_bottom";
	
	// Columns in the answerkey table
	public static final String COLUMN_ANSWER = "answer";
	
	// Columns in the scoreboard table
	public static final String COLUMN_ALBUM_ID = "album_id";
	public static final String COLUMN_TIMES_TESTED = "times_tested";
	public static final String COLUMN_BEST_TIME_IN_SEC = "best_time_in_sec";
	public static final String COLUMN_BEST_SCORE = "best_score";
	public static final String COLUMN_ALBUM_DESC = "album_desc";
	public static final String COLUMN_SCENE_DESC = "scene_desc";
	
    // These are the column names for the purchase history table. We need a
    // column named "_id" if we want to use a CursorAdapter. The primary key is
    // the orderId so that we can be robust against getting multiple messages
    // from the server for the same purchase.
    public static final String IAB_HIST_ORDER_ID_COL = "_id";
    public static final String IAB_HIST_STATE_COL = "state";
    public static final String IAB_HIST_PRODUCT_ID_COL = "productId";
    public static final String IAB_HIST_PURCHASE_TIME_COL = "transTime";
    public static final String IAB_HIST_DEVELOPER_PAYLOAD_COL = "payload";

    // These are the column names for the "purchased items" table.
    public static final String IAB_PRODUCT_ID_COL = "_id";
    public static final String IAB_QUANTITY_COL = "quantity";
    public static final String IAB_PRODUCT_DESC_COL = "description";

	public static final String SQL_SCENE_COUNT = "SELECT count(*) "
			+ " FROM scene s " + " JOIN album v ON v._id = s.album_id "
			+ " JOIN iab_asset a ON s.purchase_level = a.purchase_level "
			+ " LEFT OUTER JOIN iab i ON i._id = a._id";
}
