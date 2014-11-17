package com.cendrex.adapter;

import java.util.ArrayList;

import com.cendrex.R;
import com.cendrex.resource.AdvantagesResource;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AdvantagesAdapter extends ArrayAdapter<AdvantagesResource> {

	private LayoutInflater layoutInflater;
	private ArrayList<AdvantagesResource> listAdvantages;

	public AdvantagesAdapter(Context context, ArrayList<AdvantagesResource> listAdvantages) {
		super(context, 0, listAdvantages);
		this.listAdvantages = listAdvantages;
		this.layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			ViewHolder holder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.item_list_advantages, null);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);

			convertView.setTag(holder);
		}

		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		AdvantagesResource advantages = listAdvantages.get(position);
		viewHolder.tvTitle.setText(advantages.title);

		return convertView;
	}

	@Override
	public int getCount() {
		return listAdvantages.size();
	}

	class ViewHolder {
		TextView tvTitle;
	}
}
