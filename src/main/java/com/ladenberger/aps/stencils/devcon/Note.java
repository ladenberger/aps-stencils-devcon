package com.ladenberger.aps.stencils.devcon;

import javax.validation.constraints.NotNull;

public class Note {

	@NotNull
	private String userID;

	@NotNull
	private String fullName;

	@NotNull
	private String content;

	@NotNull
	private String date;

	public Note() {
	}

	public Note(String userID, String fullName, String content, String date) {
		this.userID = userID;
		this.fullName = fullName;
		this.content = content;
		this.date = date;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
