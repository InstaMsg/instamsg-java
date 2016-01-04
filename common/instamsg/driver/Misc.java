package common.instamsg.driver;

public interface Misc {
	
	void rebootDevice();
	String getClientSessionData();
	String getClientMetadata();
	String getNetworkData();
	String getManufacturer();
	String getDeviceUuid();
	String getDeviceIpAddress();
}
