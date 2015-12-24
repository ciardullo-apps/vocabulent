package com.ciardullo.vocabulent.vo;

import java.io.Serializable;

public abstract class CommonVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int theId;
	private String theName;
	public int getTheId() {
		return theId;
	}
	public void setTheId(int theId) {
		this.theId = theId;
	}
	public String getTheName() {
		return theName;
	}
	public void setTheName(String theName) {
		this.theName = theName;
	}
	public CommonVO() {
	}
	public CommonVO(int theId, String theName) {
		this.theId = theId;
		this.theName = theName;
	}
}
