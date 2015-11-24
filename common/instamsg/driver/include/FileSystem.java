package common.instamsg.driver.include;

public interface FileSystem {
	
	void init_file_system(String arg);
	void release_file_system();
	void release_underlying_file_system_medium_guaranteed();
	void connect_underlying_file_system_medium_guaranteed();
	void setFileName(String fileName);
	void setCallbackHandler(FileSystemCallback callback);
}
