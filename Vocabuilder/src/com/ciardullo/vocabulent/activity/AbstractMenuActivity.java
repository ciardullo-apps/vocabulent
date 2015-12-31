package com.ciardullo.vocabulent.activity;

import java.util.Deque;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ciardullo.vocabulent.AppConstants;
import com.ciardullo.vocabulent.dao.BillingDAO;
import com.ciardullo.vocabulent.util.LayoutUtil;
import com.ciardullo.vocabulent.util.SceneManager;
import com.ciardullo.vocabulent_it.R;

/**
 * Ancestor for menu and dialog aware activities in Vocabulent
 */
public class AbstractMenuActivity extends Activity implements AppConstants {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.setQwertyMode(true);
		MenuItem item = menu.add(0, MENU_ITEM_UPGRADE, 0, R.string.upgrade);
		item.setIcon(R.drawable.ic_menu_compass);
		item = menu.add(0, MENU_ITEM_ABOUT, 0, R.string.about);
		item.setIcon(R.drawable.ic_menu_help);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean b = false;
		switch (item.getItemId()) {
		case MENU_ITEM_ABOUT:
			// Create the about box dialog
			showDialog(DIALOG_ABOUT);
			b = true;
			break;
		case MENU_ITEM_UPGRADE:
			// Start the Billing activity
			startActivityForResult(new Intent(this, BillingActivity.class), 0);
			b = true;
			break;
		default:
			break;
		}
		return b;
	}

	@Override
	/**
	 * Called after startActivityForResult() from onOptionsItemSelected()
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Refresh the SceneManager if a Scene Pack purchase was made
		switch(resultCode) {
		case RESULT_OK_REFRESH_SCENE_MGR:
			// Clear SceneManager, so the purchased scenes are loaded
			SceneManager.getInstance().cleanup();
			
			// Return to StartMenuActivity so that purchased scene is picked up
			if(this instanceof StartMenuActivity) {
			} else {
				Intent intent = new Intent(this, StartMenuActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			break;
		case RESULT_OK_START_BILLING_ACTIVITY:
			// Start the Billing activity
			startActivityForResult(new Intent(this, BillingActivity.class),
					REQUEST_START_BILLING_ACTIVITY);
			break;
		case RESULT_OK:
		case RESULT_CANCELED:
			break;
		default:
			;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		switch (id) {
		case DIALOG_ABOUT:
			dialog.setContentView(R.layout.about);
			dialog.setTitle(R.string.about);
			break;
		default:
			;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_ABOUT:
			BillingDAO dao = null;
			try {
				dao = new BillingDAO(this);
				try {
					dao.open();
				} catch (Exception e) {
				}
				Deque<Object> purchasedItems = dao.getPurchasedItemsList();
				dialog.setContentView(R.layout.about);
				dialog.setTitle(R.string.about);
				LinearLayout aboutList = (LinearLayout) dialog
						.findViewById(R.id.aboutList);
				while (!purchasedItems.isEmpty()) {
					// Create a list of each expression type
					Object o = purchasedItems.pop();
					String s = o.toString();
					TextView tv = LayoutUtil
							.getATextView(getApplicationContext());
					tv.setText(s);
					aboutList.addView(tv, 4); // If about.xml layout changes,
												// change 4 here
				}
				Button btnOk = (Button) dialog.findViewById(R.id.btnOk);
				btnOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						removeDialog(DIALOG_ABOUT);
					}
				});
			} finally {
				if (dao != null) {
					dao.close();
				}
			}
			break;
		default:
			dialog = null;
		}
	}

}
