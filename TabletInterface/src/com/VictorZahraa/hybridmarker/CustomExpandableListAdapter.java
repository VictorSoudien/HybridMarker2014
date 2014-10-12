package com.VictorZahraa.hybridmarker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.HashMap;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter
{	
	private Context context;
	private List<String> listHeaders;
	private HashMap<String, List<String>> listChildren;
	
	public CustomExpandableListAdapter (Context c, List<String> headers, HashMap<String, List<String>> children)
	{
		context = c;
		listHeaders = headers;
		listChildren = children;
	}
	
	@Override
	public int getGroupCount() {
		return listHeaders.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return (listChildren.get(listHeaders.get(groupPosition))).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return listHeaders.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return (listChildren.get(listHeaders.get(groupPosition))).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		// Adapted from http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
		String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeader);
        
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
        return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		
		// Adapted from http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.listItem);
        
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon_test_marked);
        
        txtListChild.setText(childText);
        
        if (childText.length() == 9) // If the folder has been renamed to the student number
        {
        	icon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_action_accept));
        }
        else
        {
        	icon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_action_cancel));
        }
        
        /*ImageView tickView = (ImageView) convertView.findViewById(R.id.icon_test_marked);
        tickView.setVisibility(View.INVISIBLE);*/
        
        return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
