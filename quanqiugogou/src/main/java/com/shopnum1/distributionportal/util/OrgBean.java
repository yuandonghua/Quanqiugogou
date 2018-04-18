package com.shopnum1.distributionportal.util;



public class OrgBean
{
	@TreeNodeId
	private int _id;
	@TreeNodePid
	private int parentId;
	@TreeNodeLabel
	private String name;
	@TreeNodeCode
	private String code;
	@TreeNodeType
	private int type;
	@TreeNodeTypeAorB
	private int type2;
	
	public OrgBean(int _id, int parentId, String name,int type,int type2,String code)
	{
		this._id = _id;
		this.parentId = parentId;
		this.name = name;
		this.type=type;
		this.type2=type2;
		this.code=code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getType2() {
		return type2;
	}

	public void setType2(int type2) {
		this.type2 = type2;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int get_id()
	{
		return _id;
	}

	public void set_id(int _id)
	{
		this._id = _id;
	}

	public int getParentId()
	{
		return parentId;
	}

	public void setParentId(int parentId)
	{
		this.parentId = parentId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
