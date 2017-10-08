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
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import application.model.FrameSettings.FilterLine;
import application.utils.Reference;

public class FrameProcessor {

	private Mat complexImage;
	// private Mat mask;

	private List<Mat> complexSplit;
	private List<Mat> matBuffer;
	private List<Mat> bgr;

	private Mat kernel;

	private FrameUpdate frameUpdate;

	private int frameIndex = 0;
	private FrameSettings frameSettings;

	public FrameProcessor() {

		bgr = new ArrayList<>();
		complexSplit = new ArrayList<>();
		matBuffer = new ArrayList<>();

		complexImage = new Mat();
		// define kernel for sharpen
		kernel = new Mat(3, 3, CvType.CV_8S, new Scalar(-1));
		kernel.put(1, 1, 18);
		kernel.put(0, 1, -2);
		kernel.put(1, 0, -2);
		kernel.put(1, 2, -2);
		kernel.put(2, 1, -2);
	}

	/*
	 * public void setMask(Mat mask) { this.mask = mask; }
	 */
	public void process(Mat frame, int frameIndex, FrameSettings fs) {
		this.frameSettings = fs;
		this.frameIndex = frameIndex;
		frameUpdate = Reference.getFrameUpdate();

		// Remove specific Frequency disturbance
		if (frameSettings.isFFT()) {

			if (frameSettings.isRectLine()) {
				rgbFFTFilter(frame);
			} else {
				rgbFFTFilter_Line(frame);
			}
		}

		if (fs.isDenoise()) {
			// Remove High Frequency disturbance
			denoise(frame);

		}

		if (fs.isDenoiseColor()) {
			// Remove High Frequency disturbance
			denoiseColor(frame);
		}

		blurSharpen(frame, fs.getBlurSharpState());

		gammaCorrection(frame, frameSettings.getGamma());

		if (fs.isContrast()) {
			autoKontrast(frame, fs.getClipLimit(), fs.getGridSize());
		}

		if (frameUpdate != null) {
			frameUpdate.newFrameAvailable(getFrequencyDomain(frame));
		}

		if (fs.isResize()) {
			resize(frame, fs.getWidth(), fs.getHeight());
		}
	}

	public void gammaCorrection(Mat img, double gamma) {

		Mat lut = new Mat(1, 256, CvType.CV_8UC1);
		lut.setTo(new Scalar(0));

		for (int i = 0; i < 256; i++) {
			lut.put(0, i, Math.pow((double) (1.0 * i / 255), 1 / gamma) * 255);
		}

		Core.LUT(img, lut, img);

		// ++++++++++ Free Memory ++++++++++ //
		lut.release();
	}

	public void denoise(Mat img) {
		// Lowpass - Blur

		Imgproc.GaussianBlur(img, img, new Size(0, 0), 1);
		sharpen(img);
	}

	public void denoiseColor(Mat img) {

		// +++++++++++++++++++ EXPERIMENTAL +++++++++++++++++++ //

		// Imgproc.medianBlur(img, img, 5);
		// Imgproc.pyrDown(img, img);
		// Imgproc.pyrUp(img, img);
		// Imgproc.pyrMeanShiftFiltering(img, img, 0, 15);

		img.convertTo(img, CvType.CV_8UC3);

		Mat dst = new Mat();
		Imgproc.bilateralFilter(img, dst, 5, 120, 120, Core.BORDER_DEFAULT);
		Imgproc.bilateralFilter(dst, img, 5, 120, 120, Core.BORDER_DEFAULT);

		// ++++++++++ Free Memory ++++++++++ //
		dst.release();

		// +++++++++++++++++++ EXPERIMENTAL +++++++++++++++++++ //

	}

	public void blurSharpen(Mat img, int state) {
		if (state > 0) {
			for (int i = 0; i < state; i++) {
				sharpen(img);
			}
		}

		if (state < 0) {
			Imgproc.GaussianBlur(img, img, new Size(0, 0), Math.abs(state));
		}
	}

	public void sharpen(Mat img) {
		Mat div = new Mat(img.height(), img.width(), CvType.CV_32F, new Scalar(6));

		// Highpass - Sharpen
		img.convertTo(img, CvType.CV_32F);
		Imgproc.filter2D(img, img, -1, kernel);

		// Core.normalize(img, img, 0, 255, Core.NORM_MINMAX);
		Core.split(img, bgr);
		Core.divide(bgr.get(0), div, bgr.get(0));
		Core.divide(bgr.get(1), div, bgr.get(1));
		Core.divide(bgr.get(2), div, bgr.get(2));
		Core.merge(bgr, img);

		img.convertTo(img, CvType.CV_8U);

		// ++++++++++ Free Memory ++++++++++ //

		bgr.get(0).release();
		bgr.get(1).release();
		bgr.get(2).release();

		bgr.clear();

		div.release();
	}

	public void resize(Mat img, int width, int height) {
		// Resolution or ratio
		Imgproc.resize(img, img, new Size(width, height), 0, 0, Imgproc.INTER_AREA);
	}

	public Mat getFrequencyDomain(Mat img) {

		List<Mat> tmp = new ArrayList<>();
		Mat mag = new Mat();
		Mat magnitude;
		Core.split(img, bgr);
		// Blue
		transform(bgr.get(0));
		magnitude = createOptimizedMagnitude(complexImage);
		tmp.add(magnitude);

		// Green
		transform(bgr.get(1));
		magnitude = createOptimizedMagnitude(complexImage);
		tmp.add(magnitude);

		// Red
		transform(bgr.get(2));
		magnitude = createOptimizedMagnitude(complexImage);
		tmp.add(magnitude);

		Core.merge(tmp, mag);

		// ++++++++++ Free Memory ++++++++++ //
		tmp.get(0).release();
		tmp.get(1).release();
		tmp.get(2).release();
		tmp.clear();

		// System.out.println("selected " +
		// Reference.getFRQViewController().getSelectedColorModel());

		if (Reference.getFRQViewController().getSelectedColorModel() != -1) {
			Imgproc.applyColorMap(mag, mag, Reference.getFRQViewController().getSelectedColorModel());
		}

		return mag;
	}

	public void autoKontrast(Mat img, double clipLimit, Size gridSize) {
		CLAHE clahe = Imgproc.createCLAHE(clipLimit, gridSize);
		Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2Lab);
		Core.split(img, bgr);

		clahe.apply(bgr.get(0), bgr.get(0));
		// Imgproc.equalizeHist(bgr.get(0), bgr.get(0));

		Core.merge(bgr, img);
		Imgproc.cvtColor(img, img, Imgproc.COLOR_Lab2BGR);

		// ++++++++++ Free Memory ++++++++++ //
		bgr.get(0).release();
		bgr.get(1).release();
		bgr.get(2).release();
		bgr.clear();
		clahe.collectGarbage();
	}

	public void rgbFFTFilter(Mat img) {

		Core.split(img, bgr);

		// Blue
		mask(transform(bgr.get(0)), frameSettings.getMask());
		inverseTranform(complexImage, bgr.get(0));

		// Green
		mask(transform(bgr.get(1)), frameSettings.getMask());
		inverseTranform(complexImage, bgr.get(1));

		// Red
		mask(transform(bgr.get(2)), frameSettings.getMask());
		inverseTranform(complexImage, bgr.get(2));

		Core.merge(bgr, img);

		// ++++++++++ Free Memory ++++++++++ //
		bgr.get(0).release();
		bgr.get(1).release();
		bgr.get(2).release();
	}

	public void mask(Mat complexImage, Mat mask) {

		shiftDFT(complexImage); // invert quadrants

		if (mask != null) {
			Core.split(complexImage, complexSplit);

			Core.multiply(complexSplit.get(0), mask, complexSplit.get(0));
			Core.multiply(complexSplit.get(1), mask, complexSplit.get(1));

			Core.merge(complexSplit, complexImage);

			// ++++++++++ Free Memory ++++++++++ //
			complexSplit.get(0).release();
			complexSplit.get(1).release();
			complexSplit.clear();
		}

		shiftDFT(complexImage); // invert quadrants

	}

	public void rgbFFTFilter_Line(Mat img) {

		Core.split(img, bgr);

		// Blue
		applyLineFilter(transform(bgr.get(0)));
		inverseTranform(complexImage, bgr.get(0));

		// Green
		applyLineFilter(transform(bgr.get(1)));
		inverseTranform(complexImage, bgr.get(1));

		// Red
		applyLineFilter(transform(bgr.get(2)));
		inverseTranform(complexImage, bgr.get(2));

		Core.merge(bgr, img);

		// ++++++++++ Free Memory ++++++++++ //
		bgr.get(0).release();
		bgr.get(1).release();
		bgr.get(2).release();
	}

	// 0 1 2 3 4 5
	// start X , start Y || end X , end Y || width , height
	public void applyLineFilter(Mat complexImage) {

		shiftDFT(complexImage); // invert quadrants

		double fill[] = { 0, 0 };

		// 0 1 2 3
		// start X , start Y || end X , end Y
		for (FilterLine filter : frameSettings.getFilterList()) {

			int struktW = filter.getStructureElement()[0];
			int struktH = filter.getStructureElement()[1];
			int halfW = struktW / 2;
			int halfH = struktH / 2;

			int k[] = filter.getFilterPath();
			int[] pos = getFilterPos(frameIndex, filter);

			if (pos != null) {
				for (int i = pos[1] - halfH; i <= pos[1] + halfH; i++) {
					for (int j = pos[0] - halfW; j <= pos[0] + halfW; j++) {

						if ((i > 240 || i < 240) && (i > 0 && i < frameSettings.getHeight())) {
							complexImage.put(i, j, fill);
						}

					}
				}
			}

		}

		shiftDFT(complexImage); // invert quadrants

	}

	public int[] getFilterPos(int frame, FilterLine filter) {
		int[] pos = new int[2];
		int[] coor = filter.getFilterPath();
		int frameDiff = filter.getFilterEndFrame() - filter.getFilterStartFrame();
		double relativPos = (double) (frame - filter.getFilterStartFrame()) / frameDiff;

		int xDiff = (int) ((coor[0] - coor[2]) * relativPos);
		int yDiff = (int) ((coor[1] - coor[3]) * relativPos);

		pos[0] = coor[0] - xDiff;
		pos[1] = coor[1] - yDiff;

		if (frame >= filter.getFilterStartFrame() && frame <= filter.getFilterEndFrame()) {
			return pos;
		} else {
			return null;
		}

	}

	public Mat transform(Mat image) {

		// optimize the dimension of the loaded image
		image = optimizeImageDim(image);
		image.convertTo(image, CvType.CV_32F);
		// prepare the image planes to obtain the complex image
		matBuffer.add(image);
		matBuffer.add(Mat.zeros(image.size(), CvType.CV_32F));
		// prepare a complex image for performing the dft
		Core.merge(matBuffer, complexImage);
		// dft
		Core.dft(complexImage, complexImage);

		// optimize the image resulting from the dft operation
		// magnitude = createOptimizedMagnitude(complexImage);
		// ++++++++++ Free Memory ++++++++++ //
		matBuffer.get(0).release();
		matBuffer.get(1).release();
		matBuffer.clear();

		return complexImage;
	}

	private void inverseTranform(Mat complexImage, Mat restoredImage) {

		Core.idft(complexImage, complexImage);

		Core.split(complexImage, matBuffer);
		Core.normalize(matBuffer.get(0), restoredImage, 0, 255, Core.NORM_MINMAX);

		// move back the Mat to 8 bit, in order to proper show the result
		restoredImage.convertTo(restoredImage, CvType.CV_8U);

		// ++++++++++ Free Memory ++++++++++ //
		matBuffer.get(0).release();
		matBuffer.get(1).release();
		matBuffer.clear();
		complexImage.release();
	}

	private Mat optimizeImageDim(Mat image) {
		// init
		Mat padded = new Mat();
		// get the optimal rows size for dft
		int addPixelRows = Core.getOptimalDFTSize(image.rows());
		// get the optimal cols size for dft
		int addPixelCols = Core.getOptimalDFTSize(image.cols());
		// apply the optimal cols and rows size to the image
		Core.copyMakeBorder(image, padded, 0, addPixelRows - image.rows(), 0, addPixelCols - image.cols(),
				Core.BORDER_CONSTANT, Scalar.all(0));

		return padded;
	}

	private Mat createOptimizedMagnitude(Mat complexImage) {
		// init
		Mat mag = new Mat();
		// split the comples image in two planes
		Core.split(complexImage, matBuffer);
		// compute the magnitude
		Core.magnitude(matBuffer.get(0), matBuffer.get(1), mag);

		// mag = matBuffer.get(1).clone();

		// move to a logarithmic scale
		Core.add(Mat.ones(mag.size(), CvType.CV_32F), mag, mag);

		Core.log(mag, mag);
		// optionally reorder the 4 quadrants of the magnitude image
		shiftDFT(mag);
		// normalize the magnitude image for the visualization since both JavaFX
		// and OpenCV need images with value between 0 and 255
		// convert back to CV_8UC1
		mag.convertTo(mag, CvType.CV_8U);
		Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);

		// ++++++++++ Free Memory ++++++++++ //
		matBuffer.get(0).release();
		matBuffer.get(1).release();
		matBuffer.clear();

		return mag;
	}

	public static void shiftDFT(Mat image) {
		Mat sub = image.submat(new Rect(0, 0, image.cols() & -2, image.rows() & -2));
		image = sub;
		int cx = image.cols() / 2;
		int cy = image.rows() / 2;

		Mat q0 = new Mat(image, new Rect(0, 0, cx, cy));
		Mat q1 = new Mat(image, new Rect(cx, 0, cx, cy));
		Mat q2 = new Mat(image, new Rect(0, cy, cx, cy));
		Mat q3 = new Mat(image, new Rect(cx, cy, cx, cy));

		Mat tmp = new Mat();

		q0.copyTo(tmp);
		q3.copyTo(q0);
		tmp.copyTo(q3);

		q1.copyTo(tmp);
		q2.copyTo(q1);
		tmp.copyTo(q2);

		// ++++++++++ Free Memory ++++++++++ //
		q0.release();
		q1.release();
		q2.release();
		q3.release();
		tmp.release();
		sub.release();
	}

}
