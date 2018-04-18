package com.zxing.bean;

import java.io.Serializable;

public class Announcement implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String Guid;
	private String Title;
	private String Remark;
	private String CreateUser;
	private String CreateTime;
	private String ModifyUser;
	private String ModifyTime;
	private int IsDeleted;
	private String AgentID;
	public String getGuid() {
		return Guid;
	}
	public void setGuid(String guid) {
		Guid = guid;
	}
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getRemark() {
		return Remark;
	}
	public void setRemark(String remark) {
		Remark = remark;
	}
	public String getCreateUser() {
		return CreateUser;
	}
	public void setCreateUser(String createUser) {
		CreateUser = createUser;
	}
	public String getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}
	public String getModifyUser() {
		return ModifyUser;
	}
	public void setModifyUser(String modifyUser) {
		ModifyUser = modifyUser;
	}
	public String getModifyTime() {
		return ModifyTime;
	}
	public void setModifyTime(String modifyTime) {
		ModifyTime = modifyTime;
	}
	public int getIsDeleted() {
		return IsDeleted;
	}
	public void setIsDeleted(int isDeleted) {
		IsDeleted = isDeleted;
	}
	public String getAgentID() {
		return AgentID;
	}
	public void setAgentID(String agentID) {
		AgentID = agentID;
	}
	
	
}
