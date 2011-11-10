package com.adapters;

import java.util.ArrayList;


import com.data.R;
import com.generic.EndLocation;
import com.layoutextend.EndPointItem;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class EndPointListAdapter extends BaseAdapter
{
	private ArrayList<EndLocation> mEndLocations;
    private Context mContext;
    LayoutInflater mInflate;
	public EndPointListAdapter(Context mContext, ArrayList<EndLocation> mEndLocations) 
	{
		super();
		this.mContext = mContext;
		this.mEndLocations = mEndLocations;
	}
   
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EndLocation getItem(int position) 
	{
	   if(mEndLocations.get(position)!=null)
	   {
		   return mEndLocations.get(position);
	   }
	   else
		   return null;
	}
    
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
    
	public static class ViewHolder
	{
		EndPointItem myTextView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
	   View rowView;
	   rowView = convertView;
	   ViewHolder mHolder;
	   if(rowView==null)
	   {
		    mHolder = new ViewHolder();
			convertView = (EndPointItem)View.inflate(mContext,R.layout.task_list_item , null);   
		    mHolder.myTextView = (EndPointItem)convertView;
		    convertView.setTag(mHolder);
		    //keeps a reference of the saved object and when another item is taken out it can 
		    //take it out using the reference, aka getTag
		}
		else
		{
			mHolder = (ViewHolder)convertView.getTag();
		}
	    mHolder.myTextView.setTask(mEndLocations.get(position));
		return (EndPointItem)convertView;
	}

}
