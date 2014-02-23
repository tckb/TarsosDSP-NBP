package be.hogent.tarsos.dsp.ui.layers;

import java.awt.Color;
import java.awt.Graphics2D;

import be.hogent.tarsos.dsp.ui.Axis;
import be.hogent.tarsos.dsp.ui.CoordinateSystem;


public class TimeAxisLayer implements Layer {

	private int[] intervals = { 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000};
	private int intervalIndex;
	CoordinateSystem cs;
	public TimeAxisLayer(CoordinateSystem cs) {
		this.cs = cs;
	}

	public void draw(Graphics2D graphics) {
		
		// draw legend
		graphics.setColor(Color.black);
		// every second
		int minY = Math.round(cs.getMin(Axis.Y));
		int maxX = Math.round(cs.getMax(Axis.X));
		
		//float deltaX = cs.getDelta(Axis.X); //Breedte in milisec.
		int beginDrawInterval = 1000;
		intervalIndex = 0;
		int smallDrawInterval = beginDrawInterval*intervals[intervalIndex];
	
		int markerHeight = Math.round(LayerUtilities.pixelsToUnits(graphics, 8, false));
		int textOffset = Math.round(LayerUtilities.pixelsToUnits(graphics, 12, false));
		
		int smallMarkerheight = Math.round(LayerUtilities.pixelsToUnits(graphics, 4, false));
		int smallTextOffset = Math.round(LayerUtilities.pixelsToUnits(graphics, 9, false));
		
		for (int i = (int) cs.getMin(Axis.X); i < cs.getMax(Axis.X); i++) {
			if (i % (smallDrawInterval*5) == 0) {
				graphics.drawLine(i, minY, i, minY + markerHeight);
				String text = String.valueOf(i / 1000);
				LayerUtilities.drawString(graphics, text, i, minY+ textOffset, true, false,null);
			} else if (i % smallDrawInterval == 0) {
				graphics.drawLine(i, minY, i, minY + smallMarkerheight);
				String text = String.valueOf(i / 1000);
				LayerUtilities.drawString(graphics, text, i, minY + smallTextOffset, true, false,null);
			}
		}
		
		int axisLabelOffset = Math.round(LayerUtilities.pixelsToUnits(graphics, 26, true));
		textOffset = Math.round(LayerUtilities.pixelsToUnits(graphics, 14, false));
		LayerUtilities.drawString(graphics,"Time (s)",maxX-axisLabelOffset,minY + textOffset,true,true,Color.white);
	}

	@Override
	public String getName() {
		return "Time axis";
	}
}
