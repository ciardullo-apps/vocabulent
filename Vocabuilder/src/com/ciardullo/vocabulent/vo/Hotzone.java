package com.ciardullo.vocabulent.vo;

import android.graphics.Rect;

/**
 * Models attributes in the Hotzone table
 */
public class Hotzone extends CommonVO {
	private static final long serialVersionUID = 1L;

	/**
	 * The answer for this hotzone, in the installed language
	 */
	private String answer;

	/**
	 * Tracks if this hotzone was answered incorrectly in test mode
	 */
	private boolean right = true;

	/**
	 * Contains left, top, right, bottom of the hotzone in the source PNG (ie
	 * from Gimp). This Rect is used as a reference Rect when creating the View
	 * and Bitmap hotzones.
	 */
	private Rect strikeArea;

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Rect getStrikeArea() {
		return strikeArea;
	}

	public void setStrikeArea(Rect strikeArea) {
		this.strikeArea = strikeArea;
	}

	public Hotzone(int hotzoneId, String hotzoneDesc) {
		super(hotzoneId, hotzoneDesc);
	}

	public Hotzone(int hotzoneId, String answer, int left, int top, int right,
			int bottom, String hotzoneDesc) {
		super(hotzoneId, hotzoneDesc);
		this.answer = answer;
		strikeArea = new Rect(left, top, right, bottom);
	}

	@Override
	public boolean equals(Object o) {
		boolean b = false;
		if (o != null && o instanceof Hotzone) {
			Hotzone that = (Hotzone) o;
			b = getTheId() == that.getTheId()
					&& ((answer == null && that.getAnswer() == null) || (answer != null && answer
							.equalsIgnoreCase(that.getAnswer())))
					&& ((strikeArea == null && that.getStrikeArea() == null) || (strikeArea != null
							&& that.getStrikeArea() != null
							&& strikeArea.left == that.getStrikeArea().left
							&& strikeArea.top == that.getStrikeArea().top
							&& strikeArea.right == that.getStrikeArea().right && strikeArea.bottom == that
							.getStrikeArea().bottom));
		}
		return b;
	}

	@Override
	public int hashCode() {
		int n = getTheId() + (answer != null ? answer.hashCode() : 0)
				+ (strikeArea != null ? strikeArea.hashCode() : 0);

		return n;
	}

	public boolean isRight() {
		return right;
	}

	public void setWrong() {
		right = false;
	}

	public void setRight() {
		right = true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<hotzone>");
		sb.append("<hotzoneId>");
		sb.append(getTheId());
		sb.append("</hotzoneId>");
		sb.append("<hotzoneName>");
		sb.append(getTheName());
		sb.append("</hotzoneName>");
		sb.append("<answer>");
		sb.append(getAnswer());
		sb.append("</answer>");
		sb.append("<coordinates>");
		sb.append(getStrikeArea().toShortString());
		sb.append("</coordinates>");
		sb.append("</hotzone>");
		return sb.toString();
	}

}
