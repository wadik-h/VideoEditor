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

public class TimeMeasure {
	
	
	// Time Measure
	private long timeStart;
	private long timeEnd;
	private long millis;
	private int totalSeconds;
	
	public TimeMeasure() {
		
	}
	
	public void start() {
		timeStart = System.currentTimeMillis(); 
	}
	
	// multiplikator default value = 1
	public void end(int multiplikator) {
		timeEnd = System.currentTimeMillis(); 
		millis = (timeEnd - timeStart) * multiplikator;
	}
	
	public void reset() {
		timeStart = 0;
		timeEnd = 0;
		millis = 0;
		totalSeconds = 0;
	}
	
	public int getSeconds(){
		return totalSeconds;
	} 
	
	public int getMillis(){
		return (int) millis;
	} 
	
	public void putMillis(int millis) {
		this.millis = millis; 
	}
	
	public String getFormatTime(){
		totalSeconds = (int) (millis / 1000);
		int s = totalSeconds % 60;
		int m = (totalSeconds / 60) % 60;
		int h = totalSeconds / 3600;
		
		String sS = "";
		String mS = "";
		String hS = "";
		
		if(s < 10) {
			sS = "0" + s;
		}else {
			sS = "" + s;
		}

		if(m < 10) {
			mS = "0" + m;
		}else {
			mS = "" + m;
		}
		
		if(h < 10) {
			hS = "0" + h;
		}else {
			hS = "" + h;
		}
		
		return hS + ":" + mS + ":" + sS;
	}
	
	public static String formatTime(int millis){
		int totalSeconds = (int) (millis / 1000);
		int s = totalSeconds % 60;
		int m = (totalSeconds / 60) % 60;
		int h = totalSeconds / 3600;
		
		String sS = "";
		String mS = "";
		String hS = "";
		
		if(s < 10) {
			sS = "0" + s;
		}else {
			sS = "" + s;
		}

		if(m < 10) {
			mS = "0" + m;
		}else {
			mS = "" + m;
		}
		
		if(h < 10) {
			hS = "0" + h;
		}else {
			hS = "" + h;
		}
		
		return hS + ":" + mS + ":" + sS;
	}
	

}
