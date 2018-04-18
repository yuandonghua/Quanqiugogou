package com.shopnum1.distributionportal.util;

import java.util.ArrayList;
import java.util.List;

public class Node
{
	public Node()
	{
	}

	public Node(int id, int pId, String name,int type,int type2,String code)
	{
		this.id = id;//id
		this.pId = pId;//父类
		this.name = name;//文件名
		this.type=type;
		this.type2=type2;
		this.code=code;
	}



	private int id;
	/**
	 * 跟节点的pid=0
	 */
	private int pId = 0;
	private String name;
	/**
	 * 树的层级
	 */
	private int level;
	/**
	 * 是否是展开
	 */
	/**
	 * 不同类型对应不同布局
	 */
	private int type;
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

//a类还是B类
	private int type2;
	
	//这个是学院的分类的code
		private String code;
		

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



	private boolean isExpand = false;
	

	private Node parent;
	private List<Node> children = new ArrayList<Node>();

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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	

	public Node getParent()
	{
		return parent;
	}

	public void setParent(Node parent)
	{
		this.parent = parent;
	}

	public List<Node> getChildren()
	{
		return children;
	}

	public void setChildren(List<Node> children)
	{
		this.children = children;
	}

	/**
	 * 属否是根节点
	 * 
	 * @return
	 */
	public boolean isRoot()
	{
		return parent == null;
	}

	/**
	 * 判断当前父节点的收缩状态
	 * 
	 * @return
	 */
	public boolean isParentExpand()
	{
		if (parent == null)
			return false;
		return parent.isExpand();
	}

	/**
	 * 是否是叶子节点
	 * 
	 * @return
	 */
	public boolean isLeaf()
	{
		return children.size() == 0;
	}

	/**
	 * 得到当前节点的层级
	 * @return
	 */
	public int getLevel()
	{
		return parent == null ? 0 : parent.getLevel() + 1;
	}
	
	public boolean isExpand()
	{
		return isExpand;
	}

	public void setExpand(boolean isExpand)
	{
		this.isExpand = isExpand;
		
		if(!isExpand)
		{
			for(Node node :children)
			{
				node.setExpand(false);
			}
		}
	}
	
	

	public void setLevel(int level)
	{
		this.level = level;
	}

	@Override
	public String toString()
	{
		return "Node [id=" + id + ", pId=" + pId + ", name=" + name
				+ ", level=" + level + ", isExpand=" + isExpand + ", icon="
				+ "" +",Type="+type+ "]";
	}

}
