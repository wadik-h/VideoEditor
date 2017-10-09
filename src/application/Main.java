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

package application;

import java.io.File;
import java.io.IOException;

import application.utils.Reference;
import application.model.FrameSettings;
import application.model.VideoProcessor;
import application.view.ExportViewController;
import application.view.FRQViewController;
import application.view.MainViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	private Stage primaryStage;
	private Stage drawStage;
	private Stage exportStage;

	private MainViewController mainVC;
	private FRQViewController drawVC;
	private ExportViewController exportVC;
	private VideoProcessor vp;

	private static File absolutPathFinder;

	public static void main(String[] args) {
		// Load .dll Files
		absolutPathFinder = new File("dlls\\opencv_java330.dll");
		System.load(absolutPathFinder.getAbsolutePath());
		absolutPathFinder = new File("dlls\\opencv_ffmpeg330_64.dll");
		System.load(absolutPathFinder.getAbsolutePath());
		absolutPathFinder = new File("dlls\\openh264-1.6.0-win64msvc.dll");
		System.load(absolutPathFinder.getAbsolutePath());

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Video Editor");
		this.primaryStage.getIcons().add(new Image("file:resources/icon.png"));

		initApp();
	}

	public void initApp() {
		showMainView();

		vp = new VideoProcessor();

		Reference.setMainViewController(mainVC);
		Reference.setMain(this);
		Reference.setVideoProcessor(vp);
		Reference.setFrameSettings(new FrameSettings());

		mainVC.setReference();
		vp.setReference();
	}

	public Stage getStage() {
		return primaryStage;
	}

	public void closeApplication() {
		System.exit(-1);
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	// View Methods
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	public void showMainView() {

		try {
			// Lade View
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/MainView.fxml"));
			BorderPane rootLayout = (BorderPane) loader.load();

			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			mainVC = loader.getController();

			primaryStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// exit programm
		this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				System.exit(-1);
			}

		});
	}

	public void resetMainView() {
		primaryStage.close();
		closeFFTDrawView();
		initApp();
	}

	public void showFRQView() {

		this.drawStage = new Stage();
		this.drawStage.setTitle("FRQ View");
		this.drawStage.getIcons().add(new Image("file:resources/icon.png"));
		try {
			// Load View
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/FRQView.fxml"));
			AnchorPane rootLayout = (AnchorPane) loader.load();

			Scene scene = new Scene(rootLayout);

			// Change Cursor
			Image img = new Image("file:./resources/Cursor_RED.png");
			ImageCursor imgCursor = new ImageCursor(img);
			scene.setCursor(imgCursor);

			drawStage.setScene(scene);

			drawVC = loader.getController();
			drawVC.setReference();

			Reference.setFRQViewController(drawVC);

			// drawStage.setResizable(false);
			drawStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}

		this.drawStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				// Delete reference so that GUI is no more updated in background
				drawVC = null;
				Reference.setFrameUpadte(null);
			}

		});

	}

	public void closeFFTDrawView() {
		if (drawVC != null) {
			drawStage.close();
			drawVC = null;
			Reference.setFrameUpadte(null);
		}
	}

	public void showExportView() {

		this.exportStage = new Stage();
		this.exportStage.setTitle("Export Video");
		this.exportStage.getIcons().add(new Image("file:resources/icon.png"));

		try {
			// Load View
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/ExportView.fxml"));
			AnchorPane rootLayout = (AnchorPane) loader.load();

			Scene scene = new Scene(rootLayout);

			exportStage.setScene(scene);

			exportVC = loader.getController();

			exportStage.setResizable(false);
			exportStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeExportView() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				exportStage.close();
				exportStage = null;
				exportVC = null;
			}
		});
	}

}
