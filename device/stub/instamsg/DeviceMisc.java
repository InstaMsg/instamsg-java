package device.stub.instamsg;

import common.instamsg.driver.Misc;

public class DeviceMisc implements Misc {

	/**
	 * Utility-function that reboots the device.
	 */
	@Override
	public void rebootDevice() {
		
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
	
	
	/**
	 * This method returns the univerally-unique-identifier for this device.
	 */
	@Override
	public String getDeviceUuid() {
		return null;
	}
	
	
	@Override
	public String getDeviceIpAddress() {
		return "";
	}


	@Override
	public String getProvPinForNonGsmDevices() {
		return "";
	}

	/**
	 *  * This method return all possible info of a client.
	*{'imei':'value', 'manufacturer':'value', 'model':'GE-value', 'firmware_version':'value', 'client_version':'value', 'instamsg_version':'value',
	*	'programming_language':'value', 'os':'value', 'micro_controller_info': {'make':'value','mode':'value','ram':'value','rom':'value'}, 
	*	'cpu_info':{'make':'value','model':'value'}, 'memory_info':{'ram':'value','rom':'value'}, 'storage_info':{'flash':'value','external':'value'}, 'gps_info':{'make':'value','model':'value'},
	*	'network_interfaces':[{'type':'value','make':'value','model':'value','firmware_version':'value','imei':'value'}]}
	**/
	@Override
	public String getClientInfo() {
		return null;
	}
}
