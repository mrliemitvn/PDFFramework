package com.cendrex.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cendrex.R;

public class InfoFilesAdapter extends ArrayAdapter<String> {

	private LayoutInflater layoutInflater;
	private ArrayList<String> listFilesName;

	public InfoFilesAdapter(Context context, ArrayList<String> listFilesName) {
		super(context, 0, listFilesName);
		this.listFilesName = listFilesName;
		this.layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			ViewHolder holder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.item_list_info_files, null);
			holder.tvFileName = (TextView) convertView.findViewById(R.id.tvFileName);

			convertView.setTag(holder);
		}

		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		String fileName = listFilesName.get(position);
		viewHolder.tvFileName.setText(fileName);

		return convertView;
	}

	@Override
	public int getCount() {
		return listFilesName.size();
	}
	
	public ArrayList<String> getListFilesName() {
		return listFilesName;
	}

	class ViewHolder {
		TextView tvFileName;
	}
}
