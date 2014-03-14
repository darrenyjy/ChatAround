/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

import com.web2mob.ChatAround.R;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class UserItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	Drawable marker;
	
	private Paint	innerPaint, borderPaint, textPaint;
	
	public UserItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		marker = defaultMarker;
	}

	public UserItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		marker = defaultMarker;
		mContext = context;
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	 
	 private void drawInfoWindow(Canvas canvas, MapView	mapView, boolean shadow) {
		 if ( shadow) {
			 //  Skip painting a shadow in this tutorial
		 } else {
			 for (int i = 0; i < mOverlays.size(); ++i) {
				 OverlayItem overlayItem = mOverlays.get(i);
				 
				//  First determine the screen coordinates of the selected MapLocation
				Point selDestinationOffset = new Point();
				mapView.getProjection().toPixels(overlayItem.getPoint(), selDestinationOffset);
				
				//  Setup the info window with the right size & location
				int INFO_WINDOW_WIDTH = 125;
				int INFO_WINDOW_HEIGHT = 25;
				RectF infoWindowRect = new RectF(0, 0, INFO_WINDOW_WIDTH, INFO_WINDOW_HEIGHT);				
				int infoWindowOffsetX = selDestinationOffset.x - INFO_WINDOW_WIDTH/2;
				
				Bitmap markerMap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.b03);
				int infoWindowOffsetY = selDestinationOffset.y - INFO_WINDOW_HEIGHT - markerMap.getHeight();
				infoWindowRect.offset(infoWindowOffsetX, infoWindowOffsetY);
				
				//  Draw inner info window
				canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());
				
				//  Draw border for info window
				canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());
					
				//  Draw the MapLocation's name
				int TEXT_OFFSET_X = 10;
				int TEXT_OFFSET_Y = 15;
				canvas.drawText(overlayItem.getTitle(), infoWindowOffsetX + TEXT_OFFSET_X, 
						infoWindowOffsetY + TEXT_OFFSET_Y, getTextPaint());
			 }
    	}
	}
	 
		public Paint getInnerPaint() {
			if ( innerPaint == null) {
				innerPaint = new Paint();
				innerPaint.setARGB(225, 75, 75, 75); //gray
				innerPaint.setAntiAlias(true);
			}
			return innerPaint;
		}

		public Paint getBorderPaint() {
			if ( borderPaint == null) {
				borderPaint = new Paint();
				borderPaint.setARGB(255, 255, 255, 255);
				borderPaint.setAntiAlias(true);
				borderPaint.setStyle(Style.STROKE);
				borderPaint.setStrokeWidth(2);
			}
			return borderPaint;
		}

		public Paint getTextPaint() {
			if ( textPaint == null) {
				textPaint = new Paint();
				textPaint.setARGB(255, 255, 255, 255);
				textPaint.setAntiAlias(true);
			}
			return textPaint;
		}
	 
	/*@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	    if (shadow == false) {
	    	Projection projection = mapView.getProjection();
	        Point point = new Point();
	        projection.toPixels(geoPoint, point);

	        int markerRadius = 5;
	        RectF oval = new RectF(point.x-markerRadius,
	                               point.y-markerRadius,
	                               point.x+markerRadius,
	                               point.y+markerRadius);


	        canvas.drawOval(oval, paint);

	        float textWidth = paint.measureText(name);
	        float textHeight = paint.getTextSize();
	        RectF textRect = new RectF(point.x+markerRadius, point.y-textHeight,
	        point.x+markerRadius+8+textWidth, point.y+4);
	        canvas.drawText(name, point.x+markerRadius+4, point.y, paint);

	  	    }

	    super.draw(canvas, mapView, shadow);
	  }*/

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		
		/*AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();*/
		
		UsersMapView usersMapView = (UsersMapView) mContext;
		usersMapView.showPanelForUser(item.getTitle());
		return true;
	}
}
