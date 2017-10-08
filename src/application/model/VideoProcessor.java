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

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import application.Main;
import application.utils.Reference;
import application.utils.TimeMeasure;
import application.utils.Utils;
import application.view.FRQViewController;
import application.view.MainViewController;
import javafx.application.Platform;

public class VideoProcessor {

	// ***** Display vars ***** //
	private boolean isDisplayStop = false;

	// ***** Render vars ***** //
	private ExecutorService renderExecutor;
	private TimeMeasure totalTime;
	// Get available cores
	private int processors = Runtime.getRuntime().availableProcessors() - 1;

	// Status
	private int loadIndex = 0;
	private int saveIndex = 0;
	private boolean isCancel = false;

	private VideoCapture vidCapture;
	private VideoWriter vidWriter;

	private Main main;
	private MainViewController mainVC;
	private FRQViewController drawVC;
	private FrameSettings frameSettings;
	private FrameProcessor fp;

	private Mat frame = new Mat();
	private Mat inputBuffer[];
	private Mat outputBuffer[];
	private Mat backupImage;

	// skipped frames at the end | Sometimes frames at the end seems to be corrupted
	private int frameSkipped = 8; // TODO: Improve this solution

	public VideoProcessor() {
		if (processors == 0) {
			processors = 1;
		}

		fp = new FrameProcessor();

		totalTime = new TimeMeasure();
	}

	public Mat[] getOutputBuffer() {
		return outputBuffer;
	}

	public void setReference() {
		main = Reference.getMain();
		mainVC = Reference.getMainViewController();
		drawVC = Reference.getFRQViewController();
		frameSettings = Reference.getFrameSettings();
	}

	public void load(File file) {
		vidCapture = new VideoCapture(file.getAbsolutePath());
		vidCapture.read(frame);

		int frames = (int) vidCapture.get(Videoio.CV_CAP_PROP_FRAME_COUNT) - frameSkipped;
		frames = frames - (frames % processors);

		frameSettings.setBasic(frame.width(), frame.height(), vidCapture.get(Videoio.CV_CAP_PROP_FPS), frames);

		mainVC.setFramesForSlider(frameSettings.getFrames());
		inputBuffer = new Mat[frameSettings.getFrames()];
		outputBuffer = new Mat[frameSettings.getFrames()];

		showImages(0);
	}

	public void loadPic(File file) {
		frame = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
		backupImage = frame.clone();
		mainVC.setImgLeft(Utils.matToBufferedImage(frame));
		fp.process(frame, 0, frameSettings);
		mainVC.setImgRight(Utils.matToBufferedImage(frame));
	}

	public void savePic(File file) {
		fp.process(frame, 0, frameSettings);
		Imgcodecs.imwrite(file.getAbsolutePath(), frame);
	}

	public FrameSettings getFrameSettings() {
		return frameSettings;
	}

	public FrameProcessor getFrameProcessor() {
		return fp;
	}

	// Process Image Separately by Fame
	synchronized public void showImages(int frameNumber) {
		frameSettings.setFrameIndex(frameNumber);
		vidCapture.set(Videoio.CV_CAP_PROP_POS_FRAMES, frameNumber);
		vidCapture.read(frame);
		backupImage = frame.clone();
		mainVC.setImgLeft(Utils.matToBufferedImage(frame));
		fp.process(frame, frameNumber, frameSettings);
		mainVC.setImgRight(Utils.matToBufferedImage(frame));
	}

	public void refresh() {
		Mat tmp = backupImage.clone();
		fp.process(tmp, 0, frameSettings); // TODO
		mainVC.setImgRight(Utils.matToBufferedImage(tmp));
	}

	public void startDisplay() {

		while (vidCapture.get(Videoio.CV_CAP_PROP_POS_FRAMES) < frameSettings.getFrames()) {
			showImages((int) vidCapture.get(Videoio.CV_CAP_PROP_POS_FRAMES));
			mainVC.setSliderPosition((int) vidCapture.get(Videoio.CV_CAP_PROP_POS_FRAMES));

			if (isDisplayStop) {
				isDisplayStop = false;
				break;
			}

			if (vidCapture.get(Videoio.CV_CAP_PROP_POS_FRAMES) >= frameSettings.getFrames() - 1) {
				showImages(0);
				// TODO improve
			}
		}

	}

	public void pauseDisplay() {
		isDisplayStop = true;
	}

	public void stopDisplay() {
		mainVC.setSliderPosition(0);
		showImages(0);
	}

	public void render_Multithreading(File file, String extension) {
		totalTime.start();

		switch (extension) {
		case "avi":
			vidWriter = new VideoWriter(file.getAbsolutePath(), VideoWriter.fourcc('X', 'V', 'I', 'D'),
					frameSettings.getFramerate(), frameSettings.getSize());
			break;
		case "mp4":
			vidWriter = new VideoWriter(file.getAbsolutePath(), VideoWriter.fourcc('x', '2', '6', '4'),
					frameSettings.getFramerate(), frameSettings.getSize());
			break;
		case "mov":
			vidWriter = new VideoWriter(file.getAbsolutePath(), VideoWriter.fourcc('M', 'P', '4', 'V'),
					frameSettings.getFramerate(), frameSettings.getSize());
		default:
			break;
		}

		vidCapture.set(Videoio.CV_CAP_PROP_POS_FRAMES, 0);

		isCancel = false; // allow Render

		renderExecutor = Executors.newFixedThreadPool(processors);
		WorkerThread workStack[] = new WorkerThread[processors];
		int newSize = frameSettings.getFrames() / processors;
		// Create WorkerThreads
		for (int i = 0; i < processors; i++) {
			workStack[i] = new WorkerThread(newSize, i);
			renderExecutor.execute(workStack[i]);
		}

		// Invoke loading frame Thread
		load();
		// Invoke saving frame Thread
		save();

		double state[] = new double[2];

		while (saveIndex < frameSettings.getFrames() - 1) {

			if (loadIndex > 0 && saveIndex > 0) {
				state[0] = (double) loadIndex / frameSettings.getFrames();
				state[1] = (double) saveIndex / frameSettings.getFrames();
				Reference.getExportUpdate().statusUpdate(state);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void load() {
		// Push Frames into RAM
		new Thread() {
			public void run() {
				for (int i = 0; i < frameSettings.getFrames(); i++) {
					vidCapture.read(frame);
					synchronized (inputBuffer) {
						inputBuffer[i] = frame.clone();
					}

					if (i - saveIndex > 500) {
						// System.out.println("halted: " + i);

						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					loadIndex = i;

					// Interrupt rendering
					if (isCancel) {
						break;
					}
				}
			}
		}.start();
	}

	int last;

	private void save() {

		// New Thread for saving
		new Thread() {
			public void run() {

				this.setPriority(this.MIN_PRIORITY);

				for (int i = 0; i < frameSettings.getFrames(); i++) {

					synchronized (outputBuffer) {
						if (outputBuffer[i] != null) {
							vidWriter.write(outputBuffer[i]);
							outputBuffer[i].release();
						} else {
							i--;

						}
					}

					saveIndex = i;

					// Interrupt rendering
					if (isCancel) {
						break;
					}
				}

				// Export Done
				vidWriter.release();
				renderExecutor.shutdown();

				totalTime.end(1);
				Reference.getExportUpdate().totalTimeUpdate(totalTime.getFormatTime());
				main.closeExportView();

				// Show Alert and restart Application
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Utils.showInformationDialog("Export Video", "Export Done!",
								"Total Time: " + totalTime.getFormatTime());
						main.resetMainView();
					}
				});

			}
		}.start();
	}

	public void cancelRender() {
		isCancel = true;
	}

	// Inner Class - for each execution thread
	public class WorkerThread implements Runnable {

		private int threadNumber = 0;

		private Mat tmpFrame;
		private FrameProcessor fp;

		private boolean state = false;
		private boolean lock = false;

		private int size;

		public WorkerThread(int bufferSize, int threadNumber) {
			this.size = bufferSize;
			this.threadNumber = threadNumber;
			fp = new FrameProcessor();
		}

		public int getCurrentFrame() {
			return threadNumber;
		}

		public boolean getThreadState() {
			return state;
		}

		@Override
		public void run() {

			for (int i = 0; i < size; i++) {

				int frameN = (i * processors) + threadNumber;

				if (inputBuffer[frameN] != null) {

					synchronized (inputBuffer) {
						tmpFrame = inputBuffer[frameN].clone();
						inputBuffer[frameN].release();
					}

					fp.process(tmpFrame, frameN, frameSettings);

					synchronized (outputBuffer) {
						outputBuffer[frameN] = tmpFrame;
					}

				} else {
					i--;
					lock = true;
				}

				// Prevent Deadlock
				if (lock) {

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					lock = false;
				}

				// Interrupt rendering
				if (isCancel) {
					break;
				}

			}

			state = true;
		}

	}

}
