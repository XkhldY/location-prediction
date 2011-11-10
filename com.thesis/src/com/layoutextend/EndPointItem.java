package com.layoutextend;


import com.data.R;
import com.generic.EndLocation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;




public class EndPointItem extends LinearLayout {
	private CheckedTextView  mCheckBox;
	private TextView mTextView;
	private EndLocation mEndLocation;
	public EndPointItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}//the story is pretty simple, after inflating the xml than you can access its children
	//it's exactly like normal inflate when using LayoutInflater.
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mCheckBox = (CheckedTextView)findViewById(android.R.id.text1);
		mTextView = (TextView)findViewById(R.id.address_text);
	}
	public void setTask(EndLocation mEndLocation) {
		this.mEndLocation = mEndLocation;
		mCheckBox.setText(mEndLocation.getName());
		mCheckBox.setChecked(mEndLocation.isComplete());
		if(mEndLocation.hasAddress())
		{
			mTextView.setText(mEndLocation.getAddress());
			mTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			mTextView.setVisibility(View.GONE);
		}
		//mTextView.setText(mTask.getAddress());
	}
	public EndLocation getEndLocation() {
		return mEndLocation;
	}
	public void setText(EndLocation mEndLocation) 
	{
		// TODO Auto-generated method stub
	}
}