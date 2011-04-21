package net.tds.util.jmesa;

import java.io.Serializable;
import java.util.Date;

public class NewsEditorBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String displayOption;
	private String commentType;
	private String comment;
	private String resolution;
	private String createdBy;
	private String resolvedBy;
	private Date createdAt;
	private Date resolvedAt;
	
	public NewsEditorBean() {
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisplayOption() {
		return displayOption;
	}

	public void setDisplayOption(String displayOption) {
		this.displayOption = displayOption;
	}

	public String getCommentType() {
		return commentType;
	}

	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getResolvedBy() {
		return resolvedBy;
	}

	public void setResolvedBy(String resolvedBy) {
		this.resolvedBy = resolvedBy;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(Date resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

}
