package com.ciardullo.vocabulent.dao;

import java.util.ArrayDeque;
import java.util.Deque;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ciardullo.billing.BillingConstants.PurchaseState;

/**
 * The DAO to manage the history and purchases tables.
 * 
 * Extends AbstractDAO only to use its dbHelper. It does not use the abstract
 * methods.
 * 
 * Changes from SampleBilling app history table is now iab_hist developerPayload
 * column is now payload purchaseTime column is now transTime create table
 * iab_hist ( _id text primary key, state integer, productId text, payload text,
 * transTime integer);
 * 
 * purchased table is now iab create table iab ( _id text primary key, quantity
 * integer);
 */
public class BillingDAO extends AbstractDAO {
	private String[] allHistColumns = { 
			IAB_HIST_ORDER_ID_COL,
			IAB_HIST_PRODUCT_ID_COL,
			IAB_HIST_STATE_COL,
			IAB_HIST_PURCHASE_TIME_COL,
			IAB_HIST_DEVELOPER_PAYLOAD_COL };

	private String[] allPurchaseColumns = {
			IAB_PRODUCT_ID_COL,
			IAB_QUANTITY_COL,
			IAB_PRODUCT_DESC_COL };

	public BillingDAO(Context context) {
		super(context);
	}

	public String[] getAllPurchaseColumns() {
		return allPurchaseColumns;
	}

	/**
	 * Adds the given purchase information to the database and returns the total
	 * number of times that the given product has been purchased.
	 * 
	 * @param orderId
	 *            a string identifying the order
	 * @param productId
	 *            the product ID (sku)
	 * @param purchaseState
	 *            the purchase state of the product
	 * @param purchaseTime
	 *            the time the product was purchased, in milliseconds since the
	 *            epoch (Jan 1, 1970)
	 * @param developerPayload
	 *            the developer provided "payload" associated with the order
	 * @return the number of times the given product has been purchased.
	 */
	public synchronized int updatePurchase(String orderId, String productId,
			PurchaseState purchaseState, long purchaseTime,
			String developerPayload) {
		insertOrder(orderId, productId, purchaseState, purchaseTime,
				developerPayload);
		Cursor cursor = dbHelper.getDatabase().query(TBL_NM_PURCHASE_HISTORY,
				allHistColumns, IAB_HIST_PRODUCT_ID_COL + "= ?",
				new String[] { productId }, null, null, null, null);
		if (cursor == null) {
			return 0;
		}
		int quantity = 0;
		try {
			// Count the number of times the product was purchased
			while (cursor.moveToNext()) {
				int stateIndex = cursor.getInt(2);
				PurchaseState state = PurchaseState.valueOf(stateIndex);
				// TODO Change refund policy if abused
				// Note that a refunded purchase is treated as a purchase. Such
				// a friendly refund policy is nice for the user.
				if (state == PurchaseState.PURCHASED
						|| state == PurchaseState.REFUNDED) {
					quantity += 1;
				}
			}

			// Update the "purchased items" table
			updatePurchasedItem(productId, quantity);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return quantity;
	}

	/**
	 * Returns a cursor that can be used to read all the rows and columns of the
	 * "purchased items" table.
	 */
	public Cursor queryAllPurchasedItems() {
//		return dbHelper.getDatabase().query(TBL_NM_PURCHASE,
//				allPurchaseColumns, null, null, null, null, null);
		return dbHelper.getDatabase().rawQuery(
				"select a._id, a.quantity, b.description from iab a, iab_asset b where a._id = b._id order by b.purchase_level" , 
				null);
	}

	/**
	 * Inserts a purchased product into the database. There may be multiple rows
	 * in the table for the same product if it was purchased multiple times or
	 * if it was refunded.
	 * 
	 * @param orderId
	 *            the order ID (matches the value in the product list)
	 * @param productId
	 *            the product ID (sku)
	 * @param state
	 *            the state of the purchase
	 * @param purchaseTime
	 *            the purchase time (in milliseconds since the epoch)
	 * @param developerPayload
	 *            the developer provided "payload" associated with the order.
	 */
	private void insertOrder(String orderId, String productId,
			PurchaseState state, long purchaseTime, String developerPayload) {
		ContentValues values = new ContentValues();
		values.put(IAB_HIST_ORDER_ID_COL, orderId);
		values.put(IAB_HIST_STATE_COL, state.ordinal());
		values.put(IAB_HIST_PRODUCT_ID_COL, productId);
		values.put(IAB_HIST_DEVELOPER_PAYLOAD_COL, developerPayload);
		values.put(IAB_HIST_PURCHASE_TIME_COL, purchaseTime);

		SQLiteDatabase db = null;
		try {
			dbHelper.close();
			db = dbHelper.getWritableDatabase();
			db.replace(TBL_NM_PURCHASE_HISTORY, null, values);
		} finally {
			try {
				dbHelper.close();
			} catch(Exception e) {}
		}
		
	}

	/**
	 * Updates the quantity of the given product to the given value. If the
	 * given value is zero, then the product is removed from the table.
	 * 
	 * @param productId
	 *            the product to update
	 * @param quantity
	 *            the number of times the product has been purchased
	 */
	private void updatePurchasedItem(String productId, int quantity) {
		SQLiteDatabase db = null;
		try {
			dbHelper.close();
			db = dbHelper.getWritableDatabase();
	
			if (quantity == 0) {
				db.delete(TBL_NM_PURCHASE,
						IAB_PRODUCT_ID_COL + "=?", new String[] { productId });
				return;
			}
			ContentValues values = new ContentValues();
			values.put(IAB_PRODUCT_ID_COL, productId);
			values.put(IAB_QUANTITY_COL, quantity);

			db.replace(TBL_NM_PURCHASE, null, values);
		} finally {
			try {
				dbHelper.close();
			} catch(Exception e) {}
		}
	}

	@Override
	/**
	 * Unused abstract method.
	 */
	public String[] getAllColumns() {
		return null;
	}

	@Override
	/**
	 * Unused abstract method.
	 */
	public Cursor getAllRows() {
		return null;
	}

	@Override
	/**
	 * Unused abstract method.
	 */
	protected String[] getSelectionArgs() {
		return null;
	}

	/**
	 * Returns a list of purchased item descriptions.
	 * Used for the about box
	 * @return
	 */
	public Deque<Object> getPurchasedItemsList() {
		Cursor cursor = null;
		Deque<Object> purchasedItems = new ArrayDeque<Object>();
		try {
			cursor = queryAllPurchasedItems();
			while(cursor.moveToNext()) {
				purchasedItems.push(cursor.getString(2));
			}
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
		
		return purchasedItems;
	}
}
