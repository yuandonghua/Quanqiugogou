package com.shopnum1.distributionportal.util;


public class FileBean
{
	// (type= String.class)
	@TreeNodeId
	private int id;

	@TreeNodePid
	private int pId;

	@TreeNodeType
	private int type;
	
	@TreeNodeLabel
	private String label;

	@TreeNodeTypeAorB
	private int type2;
	
	@TreeNodeCode
	private String code;//这个的意思就是学院那里点了分类之后需传分类code,因此新增此字段
	
	public int getType2() {
		return type2;
	}

	public void setType2(int type2) {
		this.type2 = type2;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	private String desc;

	public FileBean(int id, int pId, String label,int type,int type2,String code)
	{
		this.id = id;
		this.pId = pId;
		this.label = label;//名称
		this.type=type;//item的类型
		this.type2=type2;//标示是A类还是B类
		this.code=code;
	}

	
	
	
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getpId()
	{
		return pId;
	}

	public void setpId(int pId)
	{
		this.pId = pId;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	// ...

}
