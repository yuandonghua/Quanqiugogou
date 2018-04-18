package com.shopnum1.distributionportal.adater;

import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.Node;
import com.shopnum1.distributionportal.util.TreeHelper;
import com.shopnum1.distributionportal.util.TreeListViewAdapter;

public class SimpleTreeListViewAdapter<T> extends TreeListViewAdapter<T> {
	private String[] tag;
	public EditText ed_min;
	public EditText ed_max;
	private long Min = 0;
	private long Max = Long.MAX_VALUE;
	private ImageView iv;
    private ImageView IV2;
	public void setMin(long Min) {
		this.Min = Min;
	}

	public void setMax(long Max) {
		this.Max = Max;
	}

	public long getMin() {
		if (ed_min != null && !ed_min.getText().toString().equals("")) {
			return Long.parseLong(ed_min.getText().toString());
		}
		return 0;
	}

	public long getMax() {
		if (ed_max != null && !ed_max.getText().toString().equals("")) {
			return Long.parseLong(ed_max.getText().toString());
		}
		return Long.MAX_VALUE;
	}

	public SimpleTreeListViewAdapter(ListView tree, Context context,
			List<T> datas, int defaultExpandLevel)
			throws IllegalArgumentException, IllegalAccessException {
		super(tree, context, datas, defaultExpandLevel, false);
	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	public void setTag(String[] tag) {
		this.tag = tag;
	}

	@Override
	public View getConvertView(Node node, int position, View convertView,
			ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			if (node.getType() == 0) {
				convertView = mInflater.inflate(R.layout.item1, parent, false);
				holder.mText = (TextView) convertView
						.findViewById(R.id.id_item_text);
			} else if (node.getType() == 1) {
				convertView = mInflater.inflate(R.layout.item2, parent, false);
				holder.mText = (TextView) convertView
						.findViewById(R.id.id_item_text1);
				holder.iv = (ImageView) convertView.findViewById(R.id.gougou);
			} else if (node.getType() == 2) {
				convertView = mInflater.inflate(R.layout.item3, parent, false);
				ed_min = (EditText) convertView.findViewById(R.id.button1);
				ed_max = (EditText) convertView.findViewById(R.id.button2);
				iv = (ImageView) convertView.findViewById(R.id.gougou);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (node.getType() == 0) {
			holder.mText.setText(node.getName());
		}
		if (node.getType() == 1) {
			holder.mText.setText(node.getName());

			if (Min == 0 && Max == Long.MAX_VALUE
					&& node.getName().equals("全部价格")) {
				holder.iv.setVisibility(View.VISIBLE);
					if(node.getpId() == 1){
						IV2=holder.iv;
					}
			
			} else if ((Min + "-" + Max).equals(node.getName())&&(ed_min.getText().toString().equals("")&&ed_max.getText().toString().equals(""))) {
				holder.iv.setVisibility(View.VISIBLE);
				if(node.getpId() == 1){
					IV2=holder.iv;
				}
			} else if (tag[0].equals(node.getName())) {
				holder.iv.setVisibility(View.VISIBLE);
				if(node.getpId() == 1){
					IV2=holder.iv;
				}
			} else if (tag[1].equals(node.getName())) {
				holder.iv.setVisibility(View.VISIBLE);
				if(node.getpId() == 1){
					IV2=holder.iv;
				}
			} else {
				holder.iv.setVisibility(View.GONE);
			}

		}
		if (node.getType() == 2) {
			ed_min.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if(count>0){
						iv.setVisibility(View.VISIBLE);
						if(IV2!=null){
							IV2.setVisibility(View.GONE);
						}
					}else{
						iv.setVisibility(View.GONE);
					}
					
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {

					if (!ed_min.getText().toString().equals("")) {
						if (Long.parseLong((ed_min.getText().toString())) >= Long.MAX_VALUE) {
							ed_min.setText(Long.MAX_VALUE + "");
						}
					}
				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});

			ed_max.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if(count>0){
						iv.setVisibility(View.VISIBLE);
						if(IV2!=null){
							IV2.setVisibility(View.GONE);
						}
					}else{
						iv.setVisibility(View.GONE);
					}
					
					
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					if (!ed_max.getText().toString().equals("")) {
						if (Long.parseLong((ed_max.getText().toString())) >= Long.MAX_VALUE) {
							ed_max.setText(Long.MAX_VALUE + "");
						}
					}
				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});

		}
		return convertView;
	}

	private class ViewHolder {
		TextView mText;
		TextView nText;
		ImageView iv;
	}

	/**
	 * 动态插入节点
	 * 
	 * @param position
	 * @param string
	 */
	public void addExtraNode(int position, String string, int type, int type2,
			String code) {
		Node node = mVisibleNodes.get(position);
		int indexOf = mAllNodes.indexOf(node);
		// Node

		Node extraNode = new Node(-1, node.getId(), string, type, type2, code);
		extraNode.setParent(node);
		node.getChildren().add(extraNode);
		mAllNodes.add(indexOf + 1, extraNode);

		mVisibleNodes = TreeHelper.filterVisibleNodes(mAllNodes);
		notifyDataSetChanged();

	}

}
