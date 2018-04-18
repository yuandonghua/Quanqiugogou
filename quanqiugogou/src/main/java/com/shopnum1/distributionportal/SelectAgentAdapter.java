package com.shopnum1.distributionportal;

import java.util.ArrayList;
import java.util.List;

import com.zxing.bean.Agent;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class SelectAgentAdapter extends BaseAdapter  {
	
	 private LayoutInflater mInflater;
	 Context context;
	 ArrayList<String> lists;
	private Handler handler;
	
	public SelectAgentAdapter(Context context,ArrayList<String> lists) {
		 mInflater = LayoutInflater.from(context);
		this.lists = lists;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lists.size();
	}

	@Override
	public Object getItem(int i) {
		// TODO Auto-generated method stub
		return lists.get(i);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		 ViewHolder holder = null;
		 if (convertView == null) {
		  convertView = mInflater.inflate(R.layout.item_select_agent_listview, parent, false); //加载布局
		  holder = new ViewHolder();
		 
		  holder.agentIdTv = (TextView) convertView.findViewById(R.id.agentIdTv);
		  convertView.setTag(holder);
		 } else { //else里面说明，convertView已经被复用了，说明convertView中已经设置过tag了，即holder
		  holder = (ViewHolder) convertView.getTag();
		 }
		 
		 String agentId = lists.get(position).toString();
		 holder.agentIdTv.setText(agentId);
		 holder.agentIdTv.setOnClickListener(new OnItemClickListener(position));
		return convertView;
	}
	
private class OnItemClickListener implements View.OnClickListener{
	int position;
	public OnItemClickListener(int position){
		this.position = position;
		
	}

	@Override
	public void onClick(View arg0) {
		String agent = lists.get(position).toString();
		Message msg = new Message();
		msg.what = 2;
		msg.obj = agent;
		handler.sendMessage(msg);
	}
	
}
	
	private class ViewHolder{
		TextView agentIdTv;
	}

	public void setHandler(Handler handler) {
		// TODO Auto-generated method stub
		this.handler  = handler;
	}

}
