package com.google.android.location.content;



import com.google.android.lib.logs.MyLogClass;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class NavigationControls implements Runnable {
	private static final int KEEP_VISIBLE_MILLIS = 4000;
	private static final boolean FADE_CONTROLS = true;
	ImageView icon;
	private final TypedArray leftIcons;
	private final TypedArray rightIcons;
	ImageView leftImage;
	ImageView rightImage;
	private int currentIcons;
	private static final Animation SHOW_NEXT_ANIMATION = new AlphaAnimation(0F,
			1F);
	private static final Animation HIDE_NEXT_ANIMATION = new AlphaAnimation(1F,
			0F);
	private static final Animation SHOW_PREV_ANIMATION = new AlphaAnimation(0F,
			1F);
	private static final Animation HIDE_PREV_ANIMATION = new AlphaAnimation(1F,
			0F);
	LayoutInflater mInflater;
	private boolean isVisible = false;
	ImageView imageViewLeft;
	ImageView imageViewRight;
    Handler handler = new Handler();
    Runnable dismissControls = new Runnable()
    {
    	@Override
    	public void run() 
    	{
    		hide();
    	}
    };
    private final Runnable touchState;
    @Override
    public void run()
	{
		touchState.run();
		mRelativeLeft.setPressed(false);
		mRelativeRight.setPressed(false);
	};
	
	RelativeLayout mRelativeLeft;
	
	RelativeLayout mRelativeRight; 
	public NavigationControls(Context context, ViewGroup container, TypedArray leftIcons, TypedArray rightIcons, Runnable touchRunnable) 
	{
		this.leftIcons = leftIcons;
		this.rightIcons = rightIcons;
		this.touchState = touchRunnable;
		if (leftIcons.length() != rightIcons.length() || leftIcons.length() < 1) 
		{
			throw new IllegalArgumentException("Invalid icons specified");
		}
		if (touchRunnable == null) 
		{
			throw new NullPointerException("Runnable cannot be null");
		}
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRelativeLeft = (RelativeLayout) mInflater.inflate(R.layout.navcontrols_layout_left, null);
		mRelativeRight = (RelativeLayout) mInflater.inflate(R.layout.navcontrols_layout_right, null);
        
		mRelativeLeft.setOnTouchListener(new View.OnTouchListener() 
        {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) 
				{
				case MotionEvent.ACTION_DOWN:
		            mRelativeLeft.setPressed(true);
		            shiftLeftLayout();
		            handler.post(NavigationControls.this);
					break;

				case MotionEvent.ACTION_UP:
					mRelativeLeft.setPressed(false);
   				    break;
				}
				return false;
			}
		});
		mRelativeRight.setOnTouchListener(new View.OnTouchListener() 
		{	
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch (event.getAction()) 
				{
				case MotionEvent.ACTION_DOWN:
					mRelativeRight.setPressed(true);
		            shiftRightLayout();
		            handler.post(NavigationControls.this);
					break;

				case MotionEvent.ACTION_UP:
					mRelativeLeft.setPressed(false);
   				    break;
				}
				return false;
			}
		});
		
		container.addView(mRelativeLeft.findViewById(R.id.nav_layout_left));
		container.addView(mRelativeRight.findViewById(R.id.nav_layout_right));
		imageViewLeft = (ImageView)mRelativeLeft.findViewById(R.id.imgview_left);
		imageViewLeft.setImageDrawable(this.leftIcons.getDrawable(0));
		imageViewLeft.setVisibility(View.VISIBLE);
		
		imageViewRight = (ImageView)mRelativeRight.findViewById(R.id.imgview_right);
		imageViewRight.setImageDrawable(this.rightIcons.getDrawable(0));
		imageViewRight.setVisibility(View.VISIBLE);
		this.currentIcons = 0;
		// container.addView();
	}
	protected void shiftRightLayout() 
	{
		currentIcons = (currentIcons + leftIcons.length() + 1)%leftIcons.length();
		imageViewLeft.setImageDrawable(leftIcons.getDrawable(currentIcons));
		imageViewRight.setImageDrawable(rightIcons.getDrawable(currentIcons));
		
	}
	protected void shiftLeftLayout() 
	{
		currentIcons = (currentIcons + leftIcons.length() - 1)%leftIcons.length();
		imageViewLeft.setImageDrawable(leftIcons.getDrawable(currentIcons));
		imageViewRight.setImageDrawable(rightIcons.getDrawable(currentIcons));
	}
	public int getCurrentIcons() 
	{
		return currentIcons;
	}

	private void keepVisible() 
	{
	    if (isVisible && FADE_CONTROLS) 
	    {
	      handler.removeCallbacks(dismissControls);
	      handler.postDelayed(dismissControls, KEEP_VISIBLE_MILLIS);
	    }
	}
	private void hide() 
	{
	    isVisible = false;
	    imageViewLeft.setAnimation(HIDE_PREV_ANIMATION);
	    HIDE_PREV_ANIMATION.setDuration(500);
	    HIDE_PREV_ANIMATION.startNow();
	    imageViewLeft.setVisibility(View.INVISIBLE);
	    imageViewRight.setAnimation(HIDE_NEXT_ANIMATION);
	    HIDE_NEXT_ANIMATION.setDuration(500);
	    HIDE_NEXT_ANIMATION.startNow();
	    imageViewRight.setVisibility(View.INVISIBLE);
	};
	public void show() {
	    if (!isVisible) {
	      SHOW_PREV_ANIMATION.setDuration(500);
	      SHOW_PREV_ANIMATION.startNow();
	      imageViewLeft.setPressed(false);
	      imageViewLeft.setAnimation(SHOW_PREV_ANIMATION);
	      imageViewLeft.setVisibility(View.VISIBLE);

	      SHOW_NEXT_ANIMATION.setDuration(500);
	      SHOW_NEXT_ANIMATION.startNow();
	      imageViewRight.setPressed(false);
	      imageViewRight.setAnimation(SHOW_NEXT_ANIMATION);
	      imageViewRight.setVisibility(View.VISIBLE);

	      isVisible = true;
	      keepVisible();
	    } 
	    else 
	    {
	      keepVisible();
	    }
	 }

	

	
}
