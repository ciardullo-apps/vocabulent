package com.ciardullo.vocabulent.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ciardullo.vocabulent.dao.LoadAlbumsDAO;
import com.ciardullo.vocabulent.dao.LoadHotzonesDAO;
import com.ciardullo.vocabulent.dao.LoadScenesDAO;
import com.ciardullo.vocabulent.vo.Album;
import com.ciardullo.vocabulent.vo.Hotzone;
import com.ciardullo.vocabulent.vo.Scene;

/**
 * A singleton that holds all albums, with all scenes, 
 * with all of their hotzones, with answers in the
 * installed language
 */
public class SceneManager {
	private static SceneManager theSceneManager = new SceneManager();
	private int countOfLoadedScenes;
	private boolean initialized = false;

	/**
	 * Synchonize access to getInstance() in the even that initialization
	 * is in progress.
	 * @param context
	 * @return
	 */
	public static SceneManager getInstance() {
		return theSceneManager;
	}

	protected Map<Integer, Album> albums;
	
	/**
	 * Returns the Map of albums
	 * @return
	 */
	public Map<Integer, Album> getAlbums() {
		return albums;
	}
	
	private SceneManager() {
	}

	/**
	 * Loads albums, scenes, hotzones and answers from the database
	 * @param context
	 */
	public void initialize(Context context) {
		if(context != null) {
			if(!isInitialized()) {
				theSceneManager.loadAlbums(context);
				setInitialized(true);
			}
		}
	}
	
	/**
	 * Loads album table
	 */
	private void loadAlbums(Context context) {
		LoadAlbumsDAO dao = null;
		Cursor cursor = null;

		albums = new HashMap<Integer, Album>();

		try {
			dao = new LoadAlbumsDAO(context);
			dao.setSelectionArgs(new String[] { 
					String.valueOf(SceneUtil.getInstalledLanguage(context)) });
			dao.open();
			cursor = dao.getAllRows();
			if(cursor.moveToFirst()) {
				do {
					String name = cursor.getString(0);
					int albumId = cursor.getInt(1);
					String imageName = cursor.getString(2);
					String albumDesc = cursor.getString(3);
					
					// Create the Album
					Album album = new Album(albumId, name, imageName, albumDesc);
					albums.put(albumId, album);
					// Get all scenes in this album
					List<Scene> albumScenes = theSceneManager.loadScenesForAlbum(context, albumId);
					album.setAllScenes(albumScenes);
					
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e("SceneManager.loadAlbums() exception", e.toString());
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (Exception e) {
			}
			try {
				if (dao != null)
					dao.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Gets all scenes for a given album id
	 */
	private List<Scene> loadScenesForAlbum(Context context, int albumId) {
		List<Scene> scenes = new ArrayList<Scene>();
		LoadScenesDAO dao = null;
		Cursor cursor = null;

		try {
			dao = new LoadScenesDAO(context);
			dao.setSelectionArgs(new String[] { 
					String.valueOf(albumId), SceneUtil.getInstalledLanguage(context) });
			dao.open();
			cursor = dao.getAllRows();
			if(cursor.moveToFirst()) {
				do {
					String name = cursor.getString(0);
					int sceneId = cursor.getInt(1);
					String imageName = cursor.getString(2);
					String sceneDesc = cursor.getString(3);
					String locked = cursor.getString(4);

					boolean purchased = false;
					if(locked != null && !"".equals(locked)) {
						purchased = true;
					}
					Scene scene = new Scene(sceneId, name, sceneDesc,
							imageName, purchased);
					scenes.add(scene);
					
					// Load all hotzones for the scene
					List<Hotzone> hotzones = theSceneManager.loadHotzonesForScene(context, sceneId);
					scene.setAllHotzones(hotzones);
					incrementCountOfLoadedScenes();
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			// Throws CursorIndexOutOfBoundsException when an album has no scenes
			Log.e("SceneManager.loadScenesForAlbum() exception", e.toString());
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (Exception e) {
			}
			try {
				if (dao != null)
					dao.close();
			} catch (Exception e) {
			}
		}
		
		return scenes;
	}

	/**
	 * Loads hotzone table
	 */
	private List<Hotzone >loadHotzonesForScene(Context context, int sceneId) {
		List<Hotzone> hotzones = new ArrayList<Hotzone>();
		LoadHotzonesDAO dao = null;
		Cursor cursor = null;

		try {
			dao = new LoadHotzonesDAO(context);
			dao.setSelectionArgs(new String[] { 
					String.valueOf(sceneId), SceneUtil.getInstalledLanguage(context) });
			dao.open();
			cursor = dao.getAllRows();
			if(cursor.moveToFirst()) {
				do {
					String hotDesc = cursor.getString(0);
					int hotId = cursor.getInt(1);
					int pointLeft = cursor.getInt(2);
					int pointTop = cursor.getInt(3);
					int pointRight = cursor.getInt(4);
					int pointBottom = cursor.getInt(5);
					String answer = cursor.getString(6);
		
					Hotzone hotzone = new Hotzone(hotId, answer, 
							pointLeft, pointTop, pointRight, pointBottom, hotDesc);
					hotzones.add(hotzone);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e("SceneManager.loadHotzonesForScene() exception", e.toString());
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (Exception e) {
			}
			try {
				if (dao != null)
					dao.close();
			} catch (Exception e) {
			}
		}
		
		return hotzones;
	}

	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Must be synchronized so that once initialized, the 
	 * waiting ReportProgress thread will be notified to
	 * dismiss the dialog
	 * @param initialized
	 */
	public synchronized void setInitialized(boolean initialized) {
		this.initialized = initialized;
		notify();
	}
	
	/**
	 * Must be synchronized so that the ReportProgress thread
	 * can wait() for the next notify()
	 * @return
	 */
	public synchronized int getCountOfLoadedScenes() {
		return countOfLoadedScenes;
	}

	/**
	 * Must be synchronized so that the ReportProgress thread
	 * can render the progress bar
	 */
	public synchronized void incrementCountOfLoadedScenes() {
		countOfLoadedScenes++;
		notify();
	}

	public void cleanup() {
		countOfLoadedScenes = 0;
		initialized = false;
		albums = null;
	}
}
