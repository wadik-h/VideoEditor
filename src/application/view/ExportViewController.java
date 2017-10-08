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

package application.view;

import application.utils.Reference;
import application.utils.TimeMeasure;
import application.model.ExportUpdate;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ExportViewController implements ExportUpdate {

	@FXML
	private Label lblEstTime;

	@FXML
	private Label lblTotalTime;

	@FXML
	private ProgressBar loadProgress;

	@FXML
	private ProgressBar saveProgress;

	// Vars for estimated time measure
	private double last = 0;
	private double diff;
	private int mill = 0;
	private int counter;

	public ExportViewController() {
		Reference.setExportUpdate(this);
	}

	@FXML
	private void initialize() {
		int processors = Runtime.getRuntime().availableProcessors() - 1;

		if (processors == 0) {
			processors = 1;
		}
		lblTotalTime.setText("Threads: " + processors);
	}

	@Override
	public void statusUpdate(double status[]) {

		counter++;

		// Funktion called every 100ms - count up to 1s and measure remaining time
		if (counter >= 10) {

			diff = status[1] - last;
			last = status[1];

			mill = (int) ((1 - status[1]) / diff) * 1000;
			this.timeUpdate(TimeMeasure.formatTime(mill));

			counter = 0;
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// Load State
				loadProgress.setProgress(status[0]);
				// Save State
				saveProgress.setProgress(status[1]);

			}
		});

	}

	@Override
	public void timeUpdate(String time) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lblEstTime.setText("Estimated Time: " + time);
			}
		});

	}

	@Override
	public void totalTimeUpdate(String time) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lblTotalTime.setText("Total Time: " + time);
			}
		});

	}

	@FXML
	private void handleCancel() {

		Reference.getVideoProcessor().cancelRender();
	}

}
