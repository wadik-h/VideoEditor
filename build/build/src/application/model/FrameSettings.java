/*
 	Copyright (C) 2017 - Wadim Halle (e-mail: wadim-h@hotmail.de)
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package application.model;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import javafx.scene.control.ChoiceBox;

/*
 * Objekt from this class includes characteristics and settings 
 * which describe a Frame and Video. If changes are made over the GUI 
 * this class will store them so the can be applied on every single
 * Frame of the video.
 */

public class FrameSettings {

	// Current processed frame
	private int frameIndex = 0;

	// Gamma correction value
	private double gamma = 1;

	//Cut video
	private int startFrame = 0;
	private int endFrame = 0;
	
	// Original Values
	private int originalWidth;
	private int originalHeight;
	private Size originalSize;
	private double originalFramerate;
	
	// Values for video export
	private int width;
	private int height;
	private Size size = new Size();
	private double framerate;
	
	// Number of total Frames
	private int frames;
	
	//FFT Filter - false = rect | true = Line
	private boolean isRectLine = false; 
	
	// Mask for filtering in frequency domain
	private Mat mask;
	
	// "line"-filter - Stores the moving-line-filter elements
	private ArrayList<FilterLine> filters = new ArrayList<>();
	
	// CLAHE
	private double clipLimit = 1.5;
	private Size gridSize = new Size(3, 3);
	
	// Denoise
	private int blurSharpState;
	
	// File State
	private boolean fileAvailable = false;
	
	// Values for render settings - Decide which feature will be enabled
	private boolean denoise = false;
	private boolean denoiseColor = false;
	private boolean resize = false;
	private boolean contrast = false;
	private boolean fft = false;
	
	public FrameSettings() {

	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// 							General Methods							//
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	
	public void setBasic(int w, int h, double framerate, int frames) {
		this.originalSize = new Size(w, h);
		this.size = new Size(w, h);
		
		this.originalWidth = w;
		this.width = w;
		
		this.originalHeight = h;
		this.height = h;
		
		this.originalFramerate = framerate;
		this.framerate = framerate;
		
		this.frames = frames;
	}

	public void setFileAvailable(boolean fileAvailable) {
		this.fileAvailable = fileAvailable;
	}
	
	public boolean isFileAvailable() {
		return fileAvailable;
	}
	
	public void setFrameIndex(int frameIndex) {
		this.frameIndex = frameIndex;
	}
	
	public int getFrameIndex() {
		return frameIndex;
	}
	
	public double getOriFramerate() {
		return originalFramerate;
	}
	
	public Size getOriSize() {
		return originalSize;
	}
	
	public int getOriWidth() {
		return originalWidth;
	}
	
	public int getOriHeight() {
		return originalHeight;
	}
	
	public int getFrames() {
		return frames;
	}
	
	public void setWidth(int w) {
		this.width = w;
		size.width = w;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setHeight(int h) {
		this.height = h;
		size.height = h;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Size getSize() {
		return size;
	}
	
	public void setFramerate(double fr) {
		this.framerate = fr;
	}

	public double getFramerate() {
		return framerate;
	}

	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// 				Feature State - Enable/Disable						//
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	
	// set States
	public void setDenoise(boolean b) {
		this.denoise = b;
	}
	
	public boolean isDenoise() {
		return denoise;
	}
	
	public void setDenoiseColor(boolean b) {
		this.denoiseColor = b;
	}
	
	public boolean isDenoiseColor() {
		return denoiseColor;
	}
	
	// +++++++++++++++++++++++++++++++++++++++
	public void setResize(boolean b) {
		this.resize = b;
	}
	
	public boolean isResize() {
		return resize;
	}
	// +++++++++++++++++++++++++++++++++++++++
	public void setContrast(boolean b) {
		this.contrast = b;
	}
	
	public boolean isContrast() {
		return contrast;
	}
	// +++++++++++++++++++++++++++++++++++++++
	public void setFFT(boolean b) {
		this.fft = b;
	}
	
	public boolean isFFT() {
		return fft;
	}
	

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// 						Effects/Filter Section						//
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	
	public void setBlurSharpState(int blurSharpState) {
		this.blurSharpState = blurSharpState;
	}
	
	public int getBlurSharpState(){
		return blurSharpState;
	}
	
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
	
	public double getGamma() {
		return gamma;
	}
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// 					Contrast (CLAHE) - Section						//
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	
	public void setClipLimit(double clipLimit) {
		this.clipLimit = clipLimit;
	}
	
	public double getClipLimit() {
		return clipLimit;
	}
	
	public void setGridSize(int size) {
		gridSize.height = size;
		gridSize.width = size;
	}
	
	public Size getGridSize() {
		return gridSize;
	}
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// 						FRQ Filter Section							//
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	// choose between filter method 
	public boolean isRectLine() {
		return isRectLine;
	}
	
	public void setRectLine(boolean isRectLine) {
		this.isRectLine = isRectLine;
	}
	
	// +++++ Rectangle +++++ //
	public void setMask(Mat mask) {
		this.mask = mask;
	}
	
	public Mat getMask() {
		return mask;
	}

	// +++++ Line - moving filter +++++ //
	public ArrayList<FilterLine> getFilterList() {
		return filters;
	}
	
	// Inner class
	public class FilterLine{
		
		// start X , start Y || end X , end Y
		int[] filterPath = new int[4];
		private int filterStartFrame;
		private int filterEndFrame;
		
		// Structure element - width | height
		private int[] structureElement = {5, 19};
		
		public FilterLine() {
			
		}
		
		public int[] getStructureElement() {
			return structureElement;
		}
		
		public void setStructureElement(int[] structureElement) {
			this.structureElement = structureElement;
		}
		
		
		public int[] getFilterPath() {
			return filterPath;
		}
		
		public void setFilterStartFrame(int filterStartFrame) {
			this.filterStartFrame = filterStartFrame;
		}
		
		public void setFilterEndFrame(int filterEndFrame) {
			this.filterEndFrame = filterEndFrame;
		}
		
		public int getFilterStartFrame() {
			return filterStartFrame;
		}

		public int getFilterEndFrame() {
			return filterEndFrame;
		}
		
	}
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// 					Cut Video (Not implemented)						//
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	
	public void setStartFrame(int startFrame) {
		this.startFrame = startFrame;
	}
	
	public int getStartFrame() {
		return startFrame;
	}
	
	public void setEndFrame(int endFrame) {
		this.endFrame = endFrame;
	}
	
	public int getEndFrame() {
		return endFrame;
	}
	
	
}
