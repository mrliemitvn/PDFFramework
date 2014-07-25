package com.cendrex.adapter;

import java.util.ArrayList;

import com.cendrex.R;
import com.cendrex.resource.TableOfContents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TableContentsAdapter extends ArrayAdapter<TableOfContents> {

	private LayoutInflater layoutInflater;
	private Context context;
	private ArrayList<TableOfContents> listTableOfContents;

	public TableContentsAdapter(Context context, ArrayList<TableOfContents> listTableOfContents) {
		super(context, 0);
		this.context = context;
		this.listTableOfContents = listTableOfContents;
		this.layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			ViewHolder holder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.item_list_table_contents, null);
			holder.tvPage = (TextView) convertView.findViewById(R.id.tvPage);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);

			convertView.setTag(holder);
		}

		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		TableOfContents tableOfContents = listTableOfContents.get(position);
		viewHolder.tvPage.setText(tableOfContents.page);
		viewHolder.tvTitle.setText(tableOfContents.title);

		return convertView;
	}

	class ViewHolder {
		TextView tvPage;
		TextView tvTitle;
	}
}
