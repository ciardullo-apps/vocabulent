package com.ciardullo.vocabulent.vo;

/**
 * Models the scoreboard table
 */
public class ScoreCard {
	private int album_id;
	private int scene_id;
	private int times_tested;
	private int time_in_sec;
	private int score;
	private int prev_best_score;
	private int prev_best_time_in_sec;
	public int getAlbum_id() {
		return album_id;
	}
	public void setAlbum_id(int album_id) {
		this.album_id = album_id;
	}
	public int getScene_id() {
		return scene_id;
	}
	public void setScene_id(int scene_id) {
		this.scene_id = scene_id;
	}
	public int getTimes_tested() {
		return times_tested;
	}
	public void setTimes_tested(int times_tested) {
		this.times_tested = times_tested;
	}
	public int getPrev_best_time_in_sec() {
		return prev_best_time_in_sec;
	}
	public void setPrev_best_time_in_sec(int prev_best_time_in_sec) {
		this.prev_best_time_in_sec = prev_best_time_in_sec;
	}
	public int getPrev_best_score() {
		return prev_best_score;
	}
	public void setPrev_best_score(int prev_best_score) {
		this.prev_best_score = prev_best_score;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getTime_in_sec() {
		return time_in_sec;
	}
	public void setTime_in_sec(int time_in_sec) {
		this.time_in_sec = time_in_sec;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<ScoreCard>");
		sb.append("<AlbumId>");
		sb.append(album_id);
		sb.append("</AlbumId>");
		sb.append("<SceneId>");
		sb.append(scene_id);
		sb.append("</SceneId>");
		sb.append("<TimesTested>");
		sb.append(times_tested);
		sb.append("</TimesTested>");
		sb.append("<Score>");
		sb.append(score);
		sb.append("</Score>");
		sb.append("<PrevBestScore>");
		sb.append(prev_best_score);
		sb.append("</PrevBestScore>");
		sb.append("<TimeInSec>");
		sb.append(time_in_sec);
		sb.append("</TimeInSec>");
		sb.append("<PrevBestTimeInSec>");
		sb.append(prev_best_time_in_sec);
		sb.append("</PrevBestTimeInSec>");
		sb.append("</ScoreCard>");
		return sb.toString();
	}
}
