package com.ciardullo.vocabulent.vo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import com.ciardullo.vocabulent.util.SceneUtil;
import com.ciardullo.vocabulent_it.R;

/**
 * Models the attributes of the scene table
 */
public class Scene extends CommonVO {
	private static final long serialVersionUID = 1L;

	/**
	 * The image name of the scene
	 */
	private String imageName;

	/**
	 * The resource id of the scene's image, found in res/raw
	 */
	private int imageResourceId;
	
	/**
	 * The language-specific scene description
	 */
	private String sceneDesc;

	/**
	 * The list of all hotzones for this scene (from the database)
	 */
	private List<Hotzone>allHotzones;
	
	/**
	 * Is this scene purchased?
	 */
	boolean purchased;

	public Scene(int svId, String svName) {
		super(svId, svName);
	}

	/**
	 * svImageName is used to map to the resource id. Must not have the file
	 * name extension.
	 * @param svId
	 * @param svName
	 * @param svDesc
	 * @param svImageName
	 */
	public Scene(int svId, String svName, String svDesc, String svImageName,
			boolean purchased) {
		super(svId, svName);
		this.sceneDesc = svDesc;
		this.imageName = svImageName;
		this.imageResourceId = 
				SceneUtil.getImageResourceId(R.raw.class, svImageName);
		this.purchased = purchased;
	}

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

	public String getSceneDesc() {
		return sceneDesc;
	}

	public void setSceneDesc(String sceneDesc) {
		this.sceneDesc = sceneDesc;
	}

	public List<Hotzone> getAllHotzones() {
		return allHotzones;
	}

	public void setAllHotzones(List<Hotzone> allHotzones) {
		this.allHotzones = allHotzones;
	}
	
	/**
	 * Returns a randomly ordered stack of the Hotzones in allHotzones
	 * @return
	 */
	public Deque<Hotzone> getHotzonesForTesting() {
		List<Hotzone> newList = new ArrayList<Hotzone>(20);
		newList.addAll(allHotzones);
		Collections.shuffle(newList);
		Deque<Hotzone> stack = new ArrayDeque<Hotzone>(newList);
		return stack;
	}
	
	public void addHotzone(Hotzone hotzone) {
		if(allHotzones == null) {
			allHotzones = new ArrayList<Hotzone>();
		}

		allHotzones.add(hotzone);
	}

	public boolean isPurchased() {
		return purchased;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<scene>");
		sb.append("<sceneId>");
		sb.append(getTheId());
		sb.append("</sceneId>");
		sb.append("<sceneName>");
		sb.append(getTheName());
		sb.append("</sceneName>");
		sb.append("<sceneDesc>");
		sb.append(getSceneDesc());
		sb.append("</sceneDesc>");
		sb.append("<imageName>");
		sb.append(getImageName());
		sb.append("</imageName>");
		for(Hotzone hotzone: getAllHotzones()) {
			sb.append(hotzone.toString());
		}
		sb.append("</scene>");
		return sb.toString();
	}
}
