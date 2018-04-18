package com.shopnum1.distributionportal.util;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class TreeListViewAdapter<T> extends BaseAdapter
{
	protected Context mContext;
	protected List<Node> mAllNodes;
	protected List<Node> mVisibleNodes;
	protected LayoutInflater mInflater;
	protected Boolean isaddHead=false;//是否添加了头部

	protected ListView mTree;

	/**
	 * 设置Node的点击回调
	 * 
	 * @author zhy
	 * 
	 */
	
	
	public interface OnTreeNodeClickListener
	{
		void onClick(Node node, int position, View view);
	}

	private OnTreeNodeClickListener mListener;

	public void setOnTreeNodeClickListener(OnTreeNodeClickListener mListener)
	{
		this.mListener = mListener;
	}

	public TreeListViewAdapter(ListView tree, Context context, List<T> datas,
			int defaultExpandLevel,Boolean flag) throws IllegalArgumentException,
			IllegalAccessException
	{
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mAllNodes = TreeHelper.getSortedNodes(datas, defaultExpandLevel);
		mVisibleNodes = TreeHelper.filterVisibleNodes(mAllNodes);
		isaddHead=flag;
		mTree = tree;

		mTree.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if(isaddHead){
					position-=1;
				}
				expandOrCollapse(position);

				if (mListener != null)
				{
					
					mListener.onClick(mVisibleNodes.get(position), position,view);
				}

			}

		});

	}

	/**
	 * 点击搜索或者展开
	 * 
	 * @param position
	 */
	private void expandOrCollapse(int position)
	{
		Node n = mVisibleNodes.get(position);
		if (n != null)
		{
			if (n.isLeaf())
				return;
			n.setExpand(!n.isExpand());
			mVisibleNodes = TreeHelper.filterVisibleNodes(mAllNodes);
			notifyDataSetChanged();
		}
	}
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		Node node = mVisibleNodes.get(position);
		return node.getType();
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return super.getViewTypeCount();
	}
	@Override
	public int getCount()
	{
		return mVisibleNodes.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mVisibleNodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Node node = mVisibleNodes.get(position);
		convertView = getConvertView(node, position, convertView, parent);
		// 设置内边距
		convertView.setPadding(node.getLevel() * 30, 3, 3, 3);
		return convertView;
	}

	public abstract View getConvertView(Node node, int position,
			View convertView, ViewGroup parent);

}
