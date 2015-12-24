package com.ciardullo.vocabulent.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the Album table, which is a album of scenes
 */
public class Album extends CommonVO {
	private static final long serialVersionUID = 1L;

	public Album(int albumId, String albumName) {
		super(albumId, albumName);
	}

	public Album(int albumId, String albumName,
			String imageName, String albumDesc) {
		super(albumId, albumName);
		this.imageName = imageName;
		this.albumDesc = albumDesc;
	}

	/**
	 * The image name of the album
	 */
	private String imageName;

	/**
	 * The resource id of the album's image, found in res/raw
	 */
	private int imageResourceId;
	
	/**
	 * The language-specific album description
	 */
	private String albumDesc;

	/**
	 * The list of all hotzones for this album (from the database)
	 */
	private List<Scene>allScenes;

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public int getImageResourceId() {
		return imageResourceId;
	}

	public void setImageResourceId(int imageResourceId) {
		this.imageResourceId = imageResourceId;
	}

	public String getAlbumDesc() {
		return albumDesc;
	}

	public void setAlbumDesc(String albumDesc) {
		this.albumDesc = albumDesc;
	}

	public List<Scene> getAllScenes() {
		return allScenes;
	}

	public void setAllScenes(List<Scene> allScenes) {
		this.allScenes = allScenes;
	}
	
	public void addScene(Scene scene) {
		if(allScenes == null) {
			allScenes = new ArrayList<Scene>();
		}
		allScenes.add(scene);
	}
	
	/**
	 * Used by the GridView gallery to return a specific scene
	 * TODO If the position in the grid view changes, you will
	 * need to use a hashCode or some other means to retrieve a
	 * specific sceen from the List
	 * @param pos
	 * @return
	 */
	public Scene getSceneAtPosition(int pos) {
		Scene scene = null;
		if(this.allScenes != null && this.allScenes.size() > 0)
			scene = allScenes.get(pos);
		return scene;
	}
	
	/**
	 * Gets a scene by its scene id
	 * @param sceneId
	 * @return
	 */
	public Scene getSceneById(int sceneId) {
		Scene scene = null;
		for(Scene s : getAllScenes()) {
			if(s.getTheId() == sceneId) {
				scene = s;
				break;
			}
		}
		return scene;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<album>");
		sb.append("<albumId>");
		sb.append(getTheId());
		sb.append("</albumId>");
		sb.append("<albumName>");
		sb.append(getTheName());
		sb.append("</albumName>");
		sb.append("<albumDesc>");
		sb.append(getAlbumDesc());
		sb.append("</albumDesc>");
		sb.append("<imageName>");
		sb.append(getImageName());
		sb.append("</imageName>");
		for(Scene scene: getAllScenes()) {
			sb.append(scene.toString());
		}
		sb.append("</album>");
		return sb.toString();
	}
}
