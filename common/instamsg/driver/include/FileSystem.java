package common.instamsg.driver.include;

public interface FileSystem {
	
	void initFileSystem(String arg);
	void releaseFileSystem();
	void releaseUnderlyingFilesystemMediumGuaranteed();
	void connectUnderlyingFilesystemMediumGuaranteed();
	void setFileName(String fileName);
	void setCallbackHandler(FileSystemCallback callback);
}
