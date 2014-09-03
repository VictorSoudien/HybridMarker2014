package com.VictorZahraa.hybridmarker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

// Code adapted from http://www.nicholasmelnick.com/entries/104

public class ScrollViewHelper extends ScrollView
{
	private OnScrollViewListner scrollViewListener;
	
	public ScrollViewHelper(Context context) {
        super(context);
    }

    public ScrollViewHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewHelper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
	public void setOnScrollViewListener (OnScrollViewListner listener)
	{
		this.scrollViewListener = listener;
	}
	
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		scrollViewListener.onScrollChanged( this, l, t, oldl, oldt );
	    super.onScrollChanged( l, t, oldl, oldt );
	}
	
	public interface OnScrollViewListner
	{
		void onScrollChanged (ScrollViewHelper scrollView, int l, int t, int prevL, int prevT);
	}
}
