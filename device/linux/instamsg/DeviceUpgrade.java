package device.linux.instamsg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import common.instamsg.driver.FileUtils;
import common.instamsg.driver.Log;
import common.instamsg.driver.Upgrade;

public class DeviceUpgrade extends Upgrade {

	static final String TEMP_FILE_NAME    =   "~instamsg";
	static final String BINARY_NAME       =   "instamsg";
	
	FileOutputStream fos = null;

	@Override
	public void prepareForNewBinaryDownload() {
		
		FileUtils.deleteFile(FILE_UPGRADE, TEMP_FILE_NAME);
		try {
			fos = new FileOutputStream(new File(TEMP_FILE_NAME));
			
		} catch (FileNotFoundException e) {
			Log.errorLog(FILE_UPGRADE + "Could not open file for writing ...");
			fos = null;
		}
	}

	@Override
	public void copyNextChar(char c) {
		
		if(fos != null) {
			try {
				fos.write(c);
				
			} catch (IOException e) {
				Log.errorLog(FILE_UPGRADE + "Could not write character [" + c + "] ...");
				tearDownBinaryDownload();
				
				fos = null;
			}
		}
	}

	@Override
	public void tearDownBinaryDownload() {
		
		if(fos != null) {
			try {
				fos.close();
				
			} catch (IOException e) {
				Log.errorLog(FILE_UPGRADE + "Could not close filestream");
			}
		}
		
		fos = null;
	}
	
	@Override
	public void removeOldExecutableBinary() {
		
		FileUtils.removeFile(BINARY_NAME);
		Log.errorLog(FILE_UPGRADE + "Old Binary [" + BINARY_NAME + "] successfully deleted.");
	}

	@Override
	public void copyNewExecutableBinaryFromTempLocation() {
		
		FileUtils.copyFile(FILE_UPGRADE, TEMP_FILE_NAME, BINARY_NAME);
		Log.infoLog(FILE_UPGRADE + "File [" + TEMP_FILE_NAME + "] successfully copied to [" + BINARY_NAME + "]");
	}

}
