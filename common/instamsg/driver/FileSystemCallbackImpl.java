package common.instamsg.driver;

import common.instamsg.driver.include.FileSystem;
import common.instamsg.driver.include.FileSystemCallback;

public class FileSystemCallbackImpl implements FileSystemCallback {

	@Override
	public int file_system_read(FileSystem fs, int len, boolean guaranteed) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int file_system_write(FileSystem fs, int len) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int renameFile(FileSystem fs, String oldPath, String newPath) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteFile(FileSystem fs, String filePath) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void getFileListing(FileSystem fs, int maxValueLenAllowed,
			String directoryPath) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getFileSize(FileSystem fs, String filepath) {
		// TODO Auto-generated method stub
		return 0;
	}

}
