package device.linux.instamsg;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.Log;
import common.instamsg.driver.Misc;

public class DeviceMisc implements Misc {

	
	public static String DEVICE_VERSION = "2.0.0";

	/**
	 * Utility-function that reboots the device.
	 */
	@Override
	public void rebootDevice() {
		
		Log.infoLog("Dummy-Rebooting Linux-Machine !!!");
		System.exit(1);
	}

	
	/**
	 * This method returns the client-network-data, in simple JSON form, of type ::
	 *
	 * {'method' : 'value', 'ip_address' : 'value', 'antina_status' : 'value', 'signal_strength' : 'value'}
	 */
	@Override
	public String getClientSessionData() {
		return null;
	}

	/**
	 * This method returns the client-network-data, in simple JSON form, of type ::
	 *
	 * {'antina_status' : 'value', 'signal_strength' : 'value'}
	 */
	@Override
	public String getNetworkData() {
		return null;
	}


	/**
	 * This method gets the device-manufacturer.
	 */
	@Override
	public String getManufacturer() {
		return null;
	}
	
	static final String INTERFACE = "wlan0";
	
	/**
	 * This method returns the univerally-unique-identifier for this device.
	 */
	@Override
	public String getDeviceUuid() {
	
		/*
		 * For our device.. we form a UUID using the laptop's wlan0 interface MAC-address.
		 */
		try {

			NetworkInterface network = NetworkInterface.getByName(INTERFACE);
			byte[] mac = network.getHardwareAddress();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));		
			}
			
			return "" + sb.toString().toLowerCase();

		} catch (SocketException e) {
			e.printStackTrace();
			
		}
		
		return null;
	}


	@Override
	public String getDeviceIpAddress() {

		return "";
	}


	@Override
	public String getProvPinForNonGsmDevices() {
		return "test";
	}
	
	/**
	 *  * This method return all possible info of a client.
	 *  
	*    {'imei':'value', 'manufacturer':'value', 'model':'GE-value', 'firmware_version':'value', 'client_version':'value', 'instamsg_version':'value',
	*	'programming_language':'value', 'os':'value', 'micro_controller_info': {'make':'value','mode':'value','ram':'value','rom':'value'}, 
	*	'cpu_info':{'make':'value','model':'value'}, 'memory_info':{'ram':'value','rom':'value'}, 'storage_info':{'flash':'value','external':'value'}, 'gps_info':{'make':'value','model':'value'},
	*	'network_interfaces':[{'type':'value','make':'value','model':'value','firmware_version':'value','imei':'value'}]}
	**/
	
	@Override
	public String getClientInfo() {
		JSONObject json = new JSONObject();
		
		json.put("client_version", InstaMsg.INSTAMSG_VERSION + "_" + DEVICE_VERSION);
		json.put("programming_language", "java");

		/*
		json.put("imei", "354789312361213");
		json.put("manufacturer", "Telit");
		json.put("model", "GE-Quad");
		json.put("firmware_version", "13.00.008");
		json.put("micro_controller_info", getMicroControllerInfo());
		
		json.put("client_version", "1.00");
		json.put("instamsg_version", "2.00.05");
		json.put("programming_language", "python");
		json.put("os", "Telit python 2.7");
		
		json.put("cpu_info", getCpuInfo());
		json.put("memory_info", getMemoryInfo());
		json.put("storage_info", getStorageInfo());
		json.put("gps_info", getGpsInfo());
		
		json.put("network_interfaces", getNwInterfaceInfo());
		*/
		
		return json.toString();
	}
	
	private HashMap<String, String> getMicroControllerInfo(){
		HashMap<String, String> microControllerInfo = new HashMap<String, String>();
		
		/*
		microControllerInfo.put("make", "Telit");
		microControllerInfo.put("mode", "Ge910");
		microControllerInfo.put("ram", "2");
		microControllerInfo.put("rom", "4");
		*/
		
		return microControllerInfo;
	}
	
	private HashMap<String, String> getCpuInfo(){
		HashMap<String, String> cpuInfo = new HashMap<String, String>();
		
		/*
		cpuInfo.put("make", "Intel");
		cpuInfo.put("model", "Gh910");
		*/
		
		return cpuInfo;
	}
	private HashMap<String, String> getMemoryInfo(){
		HashMap<String, String> info = new HashMap<String, String>();
		
		/*
		info.put("ram", "6");
		info.put("rom", "9");
		*/
		
		return info;
	}
	private HashMap<String, String> getStorageInfo(){
		HashMap<String, String> info = new HashMap<String, String>();
		
		/*
		info.put("flash", "500");
		info.put("external", "5000");
		*/
		
		return info;
	}
	private HashMap<String, String> getGpsInfo(){
		HashMap<String, String> info = new HashMap<String, String>();
		
		/*
		info.put("make", "Intel");
		info.put("model", "5.0");
		*/
		
		return info;
	}
	
	private Set<HashMap<String, String>> getNwInterfaceInfo(){
		Set<HashMap<String, String>> set = new HashSet<HashMap<String, String>>();
		HashMap<String, String> info = new HashMap<String, String>();
		
		/*
		info.put("type", "GSM");
		info.put("make", "telit");
		info.put("model", "Ge910");
		info.put("firmware_version", "13.00.008");
		info.put("imei", "354789312361213");
		*/
		
		set.add(info);
		return set;
	}
}
