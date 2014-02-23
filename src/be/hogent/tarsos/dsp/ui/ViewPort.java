package be.hogent.tarsos.dsp.ui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


public class ViewPort {
	
	private final List<ViewPortChangedListener> listeners;

	private final CoordinateSystem cs;

	
	public ViewPort(CoordinateSystem cs){
		listeners = new ArrayList<ViewPortChangedListener>();
		this.cs = cs;
	}
	
	public void addViewPortChangedListener(ViewPortChangedListener listener){
		listeners.add(listener);
	}
	
	public static interface ViewPortChangedListener{
		void viewPortChanged(ViewPort newViewPort);
	}
	
	private void viewPortChanged(){
		for(ViewPortChangedListener listener : listeners){
			listener.viewPortChanged(this);
		}
	}
	
	public void zoom(int amount, Point zoomPoint){
		//time value
		float xDelta = cs.getDelta(Axis.X);
		float newXDelta = xDelta + amount * 1000;
		if(newXDelta > 20 && newXDelta < 600000) {
			cs.setMax(Axis.X, cs.getMin(Axis.X) + newXDelta);
		}
		
		//cents value
		if(cs.getUnitsForAxis(Axis.Y) == AxisUnit.FREQUENCY){
			float yDelta = cs.getDelta(Axis.Y);
			float newYDelta = yDelta + amount * 10;
			if(newYDelta > 50 && newXDelta < 150000) {
				cs.setMax(Axis.Y, cs.getMin(Axis.Y) + newYDelta);
			}
		}
		viewPortChanged();
	}
	
	public void resetZoom(){
		if(cs.getUnitsForAxis(Axis.Y) == AxisUnit.FREQUENCY){
			cs.setMin(Axis.Y, 3600);
			cs.setMax(Axis.Y, 12800);
		}
		
		cs.setMin(Axis.X, 0);
		cs.setMax(Axis.X, 30000);
		
		viewPortChanged();
	}
	
	public void zoomToSelection(){
		if(!cs.hasStartPoint() || cs.getEndX() == Double.MAX_VALUE){
			cs.clearPoints();
			return;
		}
		double startX = cs.getStartX();
		double startY = cs.getStartY();
		double endX = cs.getEndX();
		double endY = cs.getEndY();
		cs.clearPoints();
				
		if(startX>endX){
			double temp = startX;
			startX = endX;
			endX = temp;
		}
		if(startY>endY){
			double temp = startY;
			startY = endY;
			endY = temp;
		}
		
		//do not zoom smaller than a certain threshold
		int minTimeDiff = 1500;//ms
		int minCentsDiff = 400;//cents
		if(endX-startX <= minTimeDiff){
			endX = startX + minTimeDiff;
		}
		if(endY-startY <= minCentsDiff){
			endY = startY + minCentsDiff;
		}
			
		cs.setMin(Axis.X, (float) startX);
		cs.setMax(Axis.X, (float) endX);
		
		if(cs.getUnitsForAxis(Axis.Y) == AxisUnit.FREQUENCY){
			cs.setMin(Axis.Y, (float) startY);
			cs.setMax(Axis.Y, (float) endY);
		}
		viewPortChanged();
	}
	
	public void drag(float xAmount, float yAmount){
		cs.setMin(Axis.X, cs.getMin(Axis.X) + xAmount);
		cs.setMax(Axis.X, cs.getMax(Axis.X) + xAmount);
		
		cs.setMin(Axis.Y, cs.getMin(Axis.Y) + yAmount);
		cs.setMax(Axis.Y, cs.getMax(Axis.Y) + yAmount);
		
		viewPortChanged();
	}	
}
