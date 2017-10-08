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

package application.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;

/**
 * Provide general purpose methods for handling OpenCV-JavaFX data conversion.
 * Moreover, expose some "low level" methods for matching few JavaFX behavior.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a>
 * @version 1.1 (2017-03-10)
 * @since 1.0 (2016-09-17)
 * 
 */
public final class Utils {
	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 *
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	public static Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		} catch (Exception e) {
			// show the exception details
			System.err.println("Cannot convert the Mat object:");
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Support for the {@link mat2image()} method
	 * 
	 * @param original
	 *            the {@link Mat} object in BGR or grayscale
	 * @return the corresponding {@link BufferedImage}
	 */
	public static BufferedImage matToBufferedImage(Mat original) {

		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}

		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	public static Image bufferToFXImg(BufferedImage bIMG) {
		Image image;
		image = SwingFXUtils.toFXImage(bIMG, null);

		return image;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// //
	// Dialogs //
	// //
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	public static void showErrorDialog(String title, String header, String content) {

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		// Change CSS styling - DarkTheme
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets()
				.add(Reference.getMain().getClass().getResource("view/DarkTheme.css").toExternalForm());
		dialogPane.getStyleClass().add("myDialog");

		alert.showAndWait();
	}

	public static void showInformationDialog(String title, String header, String content) {

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		// Change CSS styling - DarkTheme
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets()
				.add(Reference.getMain().getClass().getResource("view/DarkTheme.css").toExternalForm());
		dialogPane.getStyleClass().add("myDialog");

		alert.showAndWait();
	}

	public static void showWarningDialog(String title, String header, String content) {

		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		// Change CSS styling - DarkTheme
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets()
				.add(Reference.getMain().getClass().getResource("view/DarkTheme.css").toExternalForm());
		dialogPane.getStyleClass().add("myDialog");

		alert.showAndWait();
	}
}
