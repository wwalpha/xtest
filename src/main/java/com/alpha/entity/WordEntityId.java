package com.alpha.entity;
// Generated 2018/04/16 20:54:36 by Hibernate Tools 5.2.8.Final

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * WordId generated by hbm2java
 */
@Embeddable
public class WordEntityId implements java.io.Serializable {

	private String userId;
	private int wordNo;

	public WordEntityId() {
	}

	public WordEntityId(String userId, int wordNo) {
		this.userId = userId;
		this.wordNo = wordNo;
	}

	@Column(name = "USER_ID", nullable = false, length = 50)
	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "WORD_NO", nullable = false)
	public int getWordNo() {
		return this.wordNo;
	}

	public void setWordNo(int wordNo) {
		this.wordNo = wordNo;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof WordEntityId))
			return false;
		WordEntityId castOther = (WordEntityId) other;

		return ((this.getUserId() == castOther.getUserId())
				|| (this.getUserId() != null && castOther.getUserId() != null && this.getUserId().equals(castOther.getUserId())))
				&& (this.getWordNo() == castOther.getWordNo());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getUserId() == null ? 0 : this.getUserId().hashCode());
		result = 37 * result + this.getWordNo();
		return result;
	}

}
