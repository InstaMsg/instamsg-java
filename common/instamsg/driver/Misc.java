package common.instamsg.driver;

public interface Misc {
	
	void rebootDevice();
	String getClientSessionData();
	String getNetworkData();
	String getManufacturer();
	String getDeviceUuid();
	String getDeviceIpAddress();
	String getProvPinForNonGsmDevices();
	String getClientInfo();
}
