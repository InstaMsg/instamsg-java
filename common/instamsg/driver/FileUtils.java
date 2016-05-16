package common.instamsg.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import config.DeviceConstants;

public class FileUtils {
	
	public static String TEMP_FILE_NAME   = DeviceConstants.SENSEGROW_FOLDER + "temp";

	public static void removeFile(String filePath) {
		File f = new File(filePath);
		if(f.exists()) {
			f.delete();
		}
	}
	
	public static void appendLine(String module, String filePath, String line) {
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(filePath, true));
			
		} catch (IOException e) {
			Log.errorLog(module + "Could not open file [" + filePath + "] for writing config.");
			return;
		}
		
		try {
			writer.write(line + "\n");
			
		} catch (IOException e) {
			Log.errorLog(module + "Could not write.");
			
		} finally {
			
			if(writer != null) {
				try {
					writer.close();
					
				} catch (IOException e) {
					Log.errorLog(module + "Failed to properly close file [" + filePath + "]");
				}
			}
		}
	}

	public static void createEmptyFile(String module, String filePath) {	

		BufferedWriter fileWriter = null;

		try {
			fileWriter = new BufferedWriter(new FileWriter(filePath, true));

		} catch (IOException e) {
			Log.errorLog(module + "Could not open file [" + filePath + "] for writing.");
			return;

		} finally {

			if(fileWriter != null) {

				try {
					fileWriter.close();

				} catch (IOException e) {
					Log.errorLog(module + "Failed to properly close file [" + filePath + "]");
				}
			}
		}
	}
	
	public static void cleanFileReader(String module, BufferedReader reader) {
		
		if(reader != null) {
			
			try {
				reader.close();
				
			} catch (IOException e) {				
				Log.errorLog(module + "Error happened while cleaning a file-reader");
				
			}
		}
	}
	
	public static void createFileAndAddContent(String module, String content, String filePath) {
		removeFile(filePath);
		appendLine(module, filePath, content);
	}	
	
	public static void deleteFile(String module, String path) {
		File f = new File(path);
		if(f.exists() && f.isFile()) {
			f.delete();
		}
	}
	
	public static void copyFile(String module, String oldPath, String newPath) {
		try {
			Files.copy(new File(oldPath).toPath(), new File(newPath).toPath());
			
		} catch (IOException e) {
			Log.errorLog(module + "Error happened while copying file [" + oldPath + "] to [" + newPath + "]"); 
		}
	}
}
