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

import java.awt.image.BufferedImage;
import java.io.File;

import application.Main;
import application.model.FrameSettings;
import application.model.VideoProcessor;
import application.utils.Reference;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainViewController {
	// ***** FX Vars ***** //
	@FXML
	private Slider slDenoise;

	@FXML
	private CheckBox cBoxDenoise;

	@FXML
	private CheckBox cBoxDenoiseColor;

	@FXML
	private CheckBox cBoxFreqFilter;

	@FXML
	private CheckBox cBoxResize;

	@FXML
	private CheckBox cBoxContrast;

	@FXML
	private TextField txtClipLimit;

	@FXML
	private TextField txtGridSize;

	@FXML
	private Label lblGridSize;

	@FXML
	private TextField txtWidth;

	@FXML
	private TextField txtHeight;

	@FXML
	private ImageView img_left;

	@FXML
	private ImageView img_right;

	@FXML
	private Slider slFrame;

	@FXML
	private Slider slGamma;

	@FXML
	private Label lblFrame;

	// ***** General Vars ***** //
	private FileChooser fileChooser;

	private int threadState = 0;

	private Main main;
	private VideoProcessor vp;
	private FrameSettings frameSettings;

	public MainViewController() {

	}

	public void setReference() {
		main = Reference.getMain();
		vp = Reference.getVideoProcessor();
		frameSettings = Reference.getFrameSettings();
	}

	@FXML
	private void initialize() {

		img_left.setImage(new Image("file:resources/noImage.png"));
		img_left.fitWidthProperty().bind(((AnchorPane) img_left.getParent()).widthProperty());
		img_left.fitHeightProperty().bind(((AnchorPane) img_left.getParent()).widthProperty());

		img_right.setImage(new Image("file:resources/noImage.png"));
		img_right.fitWidthProperty().bind(((AnchorPane) img_left.getParent()).widthProperty());
		img_right.fitHeightProperty().bind(((AnchorPane) img_left.getParent()).widthProperty());

	}

	public void setLableText(String txt) {
		lblFrame.setText(txt);
	}

	// +++++++++++++++++ Slider +++++++++++++++++//

	public void disableSilder() {
		slFrame.setDisable(true);
	}

	public void enableSilder() {
		slFrame.setDisable(false);
	}

	public void setFramesForSlider(int frames) {
		slFrame.setMax(frames);
		slFrame.setMajorTickUnit(frames / 16);
	}

	public void setSliderPosition(int pos) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				setLableText(pos + "");
				slFrame.setValue(pos);
			}
		});
	}

	@FXML
	private void getImage() {

		if (frameSettings.isFileAvailable()) {
			vp.showImages((int) slFrame.getValue());
			setLableText((int) slFrame.getValue() + "");
		}

	}

	public void setImgLeft(BufferedImage img) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				img_left.setImage(SwingFXUtils.toFXImage(img, null));
				img_left.setPreserveRatio(true);
			}
		});
	}

	public void setImgRight(BufferedImage img) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				img_right.setImage(SwingFXUtils.toFXImage(img, null));
				img_right.setPreserveRatio(true);
			}
		});
	}

	@FXML
	private void handlePlayPause() {
		if (frameSettings.isFileAvailable()) {
			switch (threadState) {
			case 0: // RUN

				new Thread() {
					public void run() {
						vp.startDisplay();
					}
				}.start();

				threadState = 1;
				slFrame.setDisable(true);

				break;
			case 1: // HALT
				vp.pauseDisplay();

				threadState = 0;
				slFrame.setDisable(false);
				break;
			default:
				break;
			}
		}
	}

	@FXML
	private void handleStop() {

		if (frameSettings.isFileAvailable()) {
			switch (threadState) {
			case 0: // RUN
				vp.stopDisplay();

				threadState = 0;
				slFrame.setDisable(false);

				break;
			case 1: // HALT
				vp.pauseDisplay();
				vp.stopDisplay();

				threadState = 0;
				slFrame.setDisable(false);
				break;
			default:
				break;
			}
		}

	}

	@FXML
	private void handleImportImg() {

		fileChooser = new FileChooser();
		fileChooser.setTitle("Bild öffnen");

		File input = fileChooser.showOpenDialog(main.getStage());

		if (input != null) {
			vp.loadPic(input);
		}
	}

	@FXML
	private void handleExportImg() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Image");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("*.jpg", "*.jpg"),
				new ExtensionFilter("*.png", "*.png"), new ExtensionFilter("*.tif", "*.tif"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showSaveDialog(main.getStage());

		if (selectedFile != null) {
			vp.savePic(selectedFile);
		}

	}

	@FXML
	private void handelImportVid() {
		fileChooser = new FileChooser();
		fileChooser.setTitle("Video öffnen");

		File input = fileChooser.showOpenDialog(main.getStage());
		if (input != null) {
			vp.load(input);
			frameSettings.setFileAvailable(true);
		}

	}

	@FXML
	private void handelExportVid() {

		if (frameSettings.isFileAvailable()) {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Video File");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("*.avi", "*.avi"),
					new ExtensionFilter("*.mp4", "*.mp4"), new ExtensionFilter("*.mov", "*.mov"),
					new ExtensionFilter("All Files", "*.*"));
			File selectedFile = fileChooser.showSaveDialog(main.getStage());

			if (selectedFile != null) {
				new Thread() {
					public void run() {

						vp.render_Multithreading(selectedFile,
								fileChooser.getSelectedExtensionFilter().getDescription().substring(2));
					}
				}.start();
			}

			main.showExportView();

		}
	}

	@FXML
	private void handleOpenFreqDomain() {
		main.showFRQView();
	}

	@FXML
	private void handleContrast() {
		frameSettings.setContrast(cBoxContrast.isSelected());

		// Contrast
		frameSettings.setClipLimit(Double.parseDouble(txtClipLimit.getText()));
		frameSettings.setGridSize(Integer.parseInt(txtGridSize.getText()));
		lblGridSize.setText("x " + txtGridSize.getText());

		if (threadState == 0) { // Halted
			vp.refresh();
		}
	}

	@FXML
	private void handleDenoise() {
		frameSettings.setDenoise(cBoxDenoise.isSelected());

		if (threadState == 0) { // Halted
			vp.refresh();
		}
	}

	@FXML
	private void handleDenoiseColor() {
		frameSettings.setDenoiseColor(cBoxDenoiseColor.isSelected());

		if (threadState == 0) { // Halted
			vp.refresh();
		}
	}

	@FXML
	private void handleResize() {
		frameSettings.setResize(cBoxResize.isSelected());
		frameSettings.setHeight(Integer.parseInt(txtHeight.getText()));
		frameSettings.setWidth(Integer.parseInt(txtWidth.getText()));

		if (threadState == 0) { // Halted
			vp.refresh();
		}
	}

	@FXML
	private void handleFFT() {
		frameSettings.setFFT(cBoxFreqFilter.isSelected());

		if (threadState == 0) { // Halted
			vp.refresh();
		}
	}

	@FXML
	private void handleDenoiseSilder() {
		frameSettings.setBlurSharpState((int) slDenoise.getValue());

		if (threadState == 0) { // Halted
			vp.refresh();
		}
	}

	@FXML
	private void handleGammaSlider() {
		frameSettings.setGamma(slGamma.getValue());
		if (threadState == 0) { // Halted
			vp.refresh();
		}
	}

	@FXML
	private void handleClose() {
		main.closeApplication();
	}

	@FXML
	private void handleReset() {
		main.resetMainView();
	}

}
