package net.tds.util.jmesa;

import java.io.Serializable;
import java.util.Date;

public class PersonBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private long userLoginId;
	private String firstName;
	private String lastName;
	private String userLogin;
	private Date dateCreated;
	private Date lastUpdated;
	private Integer modelScore;
	
	public Integer getModelScore() {
		return modelScore;
	}

	public void setModelScore(Integer modelScore) {
		this.modelScore = modelScore;
	}

	public PersonBean() {
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public long getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(long userLoginId) {
		this.userLoginId = userLoginId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
