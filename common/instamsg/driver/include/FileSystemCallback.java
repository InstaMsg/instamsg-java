package common.instamsg.driver.include;

public interface FileSystemCallback {
	
	int file_system_read (FileSystem fs, int len, boolean guaranteed);
	int file_system_write(FileSystem fs, int len);
	int renameFile(FileSystem fs, String oldPath, String newPath);
	int deleteFile(FileSystem fs, String filePath);
	void getFileListing(FileSystem fs, int maxValueLenAllowed, String directoryPath);
	long getFileSize(FileSystem fs, String filepath);

}
