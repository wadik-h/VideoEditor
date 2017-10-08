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

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import application.Main;
import application.model.FrameSettings;
import application.model.FrameSettings.FilterLine;
import application.model.FrameUpdate;
import application.model.VideoProcessor;
import application.utils.Reference;
import application.utils.Utils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class FRQViewController implements FrameUpdate {

	@FXML
	private ChoiceBox<String> cBox;

	@FXML
	private ToggleButton btnRect;

	@FXML
	private ToggleButton btnLine;

	@FXML
	private TextField txtSEWidth;

	@FXML
	private TextField txtSEHeight;

	@FXML
	private ImageView imgFFT;

	@FXML
	private Canvas imgDraw;

	private GraphicsContext graphic;

	private Main main;
	private VideoProcessor vp;
	private FrameSettings frameSettings;

	private Mat mask;

	private int startX, startY, endX, endY;

	private int width = 640, height = 480;

	private int posX, posY;

	private EventHandler<MouseEvent> onMousePressed;
	private EventHandler<MouseEvent> rectOnMouseDragged;
	private EventHandler<MouseEvent> rectOnMouseReleased;

	private EventHandler<MouseEvent> lineOnMouseClicked;

	// State for "line"-filter
	private int state = 0;

	public FRQViewController() {
		/*
		 * width = frameSettings.getOriWidth(); hight = frameSettings.getOriHight();
		 */
		Reference.setFrameUpadte(this);
	}

	public void setReference() {
		main = Reference.getMain();
		vp = Reference.getVideoProcessor();
		frameSettings = Reference.getFrameSettings();
	}

	public int getSelectedColorModel() {
		return cBox.getSelectionModel().getSelectedIndex() - 1;
	}

	@FXML
	private void initialize() {

		cBox.getItems().addAll("DEFAULT", "AUTUMN", "BONE", "JET", "WINTER", "RAINBOW", "OCEAN", "SUMMER", "SPRING",
				"COOL", "HSV", "PINK", "HOT", "PARULA");
		cBox.getSelectionModel().selectFirst();

		btnRect.setGraphic(new ImageView(new Image("file:resources/rectangle20.png")));
		btnLine.setGraphic(new ImageView(new Image("file:resources/line20.png")));

		mask = new Mat(height, width, CvType.CV_32F, new Scalar(1));

		graphic = imgDraw.getGraphicsContext2D();
		graphic.setStroke(Color.RED);
		graphic.setLineWidth(2);

		FrameSettings fs = new FrameSettings();

		lineOnMouseClicked = new EventHandler<MouseEvent>() {

			FilterLine filter = fs.new FilterLine();
			FilterLine filterInvert = fs.new FilterLine();

			int[] sE = new int[2];

			@Override
			public void handle(MouseEvent event) {

				// get mouse position
				posX = (int) event.getX();
				posY = (int) event.getY();

				graphic.setFill(Color.RED);

				switch (state) {
				case 0: // pending

					break;
				case 1: // beginn
					// Draw filter

					filter = fs.new FilterLine();
					filterInvert = fs.new FilterLine();

					graphic.fillRect(posX - 2, posY - 2, 3, 3);
					graphic.beginPath();
					graphic.moveTo(posX, posY);

					graphic.fillText(frameSettings.getFrameIndex() + "", posX + 5, posY);

					// Apply Filter

					filter.getFilterPath()[0] = posX - 1; // offset to match view
					filter.getFilterPath()[1] = posY;

					filterInvert.getFilterPath()[0] = width - posX + 1;// offset to match view
					filterInvert.getFilterPath()[1] = height - posY;

					filter.setFilterStartFrame(frameSettings.getFrameIndex());
					filterInvert.setFilterStartFrame(frameSettings.getFrameIndex());

					state = 2;
					break;
				case 2: // end

					// Draw filter

					graphic.fillRect(posX - 2, posY - 2, 3, 3);
					graphic.lineTo(posX, posY);
					graphic.stroke();
					graphic.closePath();
					graphic.save();

					graphic.fillText(frameSettings.getFrameIndex() + "", posX + 5, posY);

					// Apply Filter

					filter.getFilterPath()[2] = posX - 1;// offset to match view
					filter.getFilterPath()[3] = posY;

					filterInvert.getFilterPath()[2] = width - posX + 1;// offset to match view
					filterInvert.getFilterPath()[3] = height - posY;

					filter.setFilterEndFrame(frameSettings.getFrameIndex());
					filterInvert.setFilterEndFrame(frameSettings.getFrameIndex());

					// process structur element
					sE[0] = Integer.parseInt(txtSEWidth.getText());
					sE[1] = Integer.parseInt(txtSEHeight.getText());

					filter.setStructureElement(sE.clone());
					filterInvert.setStructureElement(sE.clone());

					frameSettings.getFilterList().add(filter);
					frameSettings.getFilterList().add(filterInvert);

					btnLine.setSelected(false);

					state = 0;
					break;
				default:
					break;
				}

			}
		};

		onMousePressed = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				startX = (int) event.getX();
				startY = (int) event.getY();

				graphic.setFill(Color.TRANSPARENT);
			}
		};

		rectOnMouseDragged = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				endX = (int) event.getX();
				endY = (int) event.getY();

				// Clear Rect
				graphic.clearRect(startX, startY, endX - startX + 5, endY - startY + 5);
				graphic.strokeRect(startX, startY, endX - startX, endY - startY);
				graphic.clearRect(startX + 1, startY + 1, endX - startX - 1, endY - startY - 1);

				// cross mirrored Rect
				graphic.clearRect(width - endX - 5, height - endY - 5, endX - startX + 6, endY - startY + 6);
				graphic.strokeRect(width - endX, height - endY, endX - startX, endY - startY);
				graphic.clearRect(width - endX + 1, height - endY + 1, endX - startX - 1, endY - startY - 1);

			}
		};

		rectOnMouseReleased = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				endX = (int) event.getX();
				endY = (int) event.getY();

				if (endX < 0) {
					endX = 0;
				}

				if (endY < 0) {
					endY = 0;
				}

				if (endX > width) {
					endX = width;
				}

				if (endY > height) {
					endY = height;
				}

				fillRect(mask, startX, startY, endX - startX, endY - startY);
				// invert
				fillRect(mask, width - endX, height - endY, endX - startX, endY - startY);

				// Apply Mask
				frameSettings.setMask(mask);
			}
		};

	}

	@FXML
	private void handleRect() {

		frameSettings.setRectLine(true);

		if (btnRect.isSelected()) {
			// clear Line feature
			btnLine.setSelected(false);
			btnLine.setDisable(true);
			imgDraw.setOnMouseClicked(null);

			imgDraw.setOnMousePressed(onMousePressed);
			imgDraw.setOnMouseDragged(rectOnMouseDragged);
			imgDraw.setOnMouseReleased(rectOnMouseReleased);

		} else {
			btnLine.setDisable(false);

			imgDraw.setOnMousePressed(null);
			imgDraw.setOnMouseDragged(null);
			imgDraw.setOnMouseReleased(null);
		}

	}

	@FXML
	private void handleLine() {

		frameSettings.setRectLine(false);

		if (btnLine.isSelected()) {
			// clear Rect feature
			btnRect.setSelected(false);
			btnRect.setDisable(true);
		} else {
			btnRect.setDisable(false);
		}

		imgDraw.setOnMouseClicked(lineOnMouseClicked);

		switch (state) {
		case 0: // pending

			state = 1;

			break;
		case 1: // beginn

			state = 0;
			break;
		case 2: // end
			btnLine.setSelected(true);
			break;
		default:
			break;
		}

	}

	// Draw Mask on Mat object
	public void fillRect(Mat img, int startX, int startY, int w, int h) {
		for (int y = startY; y <= (startY + h); y++) {
			for (int x = startX; x <= (startX + w); x++) {

				img.put(y, x, 0);
			}
		}
	}

	public Mat getMask() {
		return mask;
	}

	@FXML
	private void handelClear() {
		graphic.clearRect(0, 0, width, height);
	}

	@FXML
	private void handleReset() {
		graphic.clearRect(0, 0, width, height);
		mask = new Mat(height, width, CvType.CV_32F, new Scalar(1));

		btnRect.setDisable(false);
		btnLine.setDisable(false);

		frameSettings.setMask(mask);
		frameSettings.getFilterList().clear();
	}

	@Override
	public void newFrameAvailable(Mat frame) {
		imgFFT.setImage(Utils.mat2Image(frame));
	}

}
