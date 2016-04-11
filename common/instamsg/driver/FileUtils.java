package common.instamsg.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
	
	public static String TEMP_FILE_NAME   = "/home/sensegrow/temp";

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
}
