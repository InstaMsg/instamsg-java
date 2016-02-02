package device.linux.instamsg.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import common.instamsg.driver.Log;

public class FileUtils {
	
	public static String TEMP_FILE_NAME   = "/home/sensegrow/temp";

	public static void appendLine(String module, String filePath, String line) {
		
		BufferedWriter configWriter = null;
		
		try {
			configWriter = new BufferedWriter(new FileWriter(filePath, true));
			
		} catch (IOException e) {
			Log.errorLog(module + "Could not open file [" + filePath + "] for writing config.");
			return;
		}
		
		try {
			configWriter.write(line + "\n");
			
		} catch (IOException e) {
			Log.errorLog(module + "Could not write config.");
			
		} finally {
			
			if(configWriter != null) {
				try {
					configWriter.close();
					
				} catch (IOException e) {
					Log.errorLog(module + "Failed to properly close config-file");
				}
			}
		}
	}

	public static void createEmptyFile(String module, String filePath) {	

		BufferedWriter configWriter = null;

		try {
			configWriter = new BufferedWriter(new FileWriter(filePath, true));

		} catch (IOException e) {
			Log.errorLog(module + "Could not open file [" + filePath + "] for writing config.");
			return;

		} finally {

			if(configWriter != null) {

				try {
					configWriter.close();

				} catch (IOException e) {
					Log.errorLog(module + "Failed to properly close config-file");
				}
			}
		}
	}
	
	public static void cleanFileReader(String module, BufferedReader configReader) {
		
		if(configReader != null) {
			
			try {
				configReader.close();
				
			} catch (IOException e) {				
				Log.errorLog(module + "Error happened while cleaning a file-reader");
				
			}
		}
	}
}
