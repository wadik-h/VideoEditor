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

import application.Main;
import application.model.ExportUpdate;
import application.model.FrameSettings;
import application.model.FrameUpdate;
import application.model.VideoProcessor;
import application.view.FRQViewController;
import application.view.MainViewController;

/*
 * This class provides references through the whole program to make it easier to 
 * communicate between objects
 */

public class Reference {
	
	private static Main main;
	private static MainViewController mainVC;
	private static FRQViewController drawVC;
	private static VideoProcessor vp;
	private static FrameSettings frameSettings;
	private static FrameUpdate frameUpdate;
	private static ExportUpdate exportUpdate;
//	private Settings settings;
	
	public Reference(){
		
	}
	
	//---------------- Setter ----------------//
	public static void setExportUpdate(ExportUpdate exportUpdate) {
		Reference.exportUpdate = exportUpdate;
	}
	
	public static void setFrameUpadte(FrameUpdate frameUpdate) {
		Reference.frameUpdate = frameUpdate;
	}
	
	public static void setMain(Main main){
		Reference.main = main;
	}
	
	public static void setMainViewController(MainViewController mainVC){
		Reference.mainVC = mainVC;
	}
	
	public static void setFRQViewController(FRQViewController drawVC){
		Reference.drawVC = drawVC;
	}
	
	public static void setVideoProcessor(VideoProcessor vp) {
		Reference.vp = vp;
	}
	
	public static void setFrameSettings(FrameSettings frameSettings) {
		Reference.frameSettings = frameSettings;
	}

	
	/*
	public void setStettings(Settings settings){
		this.settings = settings;
	}
	*/
	//---------------- Getter ----------------//
	
	public static ExportUpdate getExportUpdate() {
		return exportUpdate;
	}
	
	public static FrameUpdate getFrameUpdate() {
		return frameUpdate;
	}
	
	public static Main getMain(){
		return main;
	}
	
	public static MainViewController getMainViewController(){
		return mainVC;
	}
	
	public static FRQViewController getFRQViewController(){
		return drawVC;
	}

	public static VideoProcessor getVideoProcessor() {
		return vp;
	}
	
	public static FrameSettings getFrameSettings() {
		return frameSettings;
	}
	
	/*
	public Settings getSettings(){
		return settings;
	}
	*/

}
