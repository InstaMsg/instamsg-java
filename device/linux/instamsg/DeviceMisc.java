package device.linux.instamsg;

import java.net.NetworkInterface;
import java.net.SocketException;

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
	 * {'imei' : 'value', 'serial_number' : 'value', 'model' : 'value', 'firmware_version' : 'value', 'manufacturer' : 'value', 'client_version' : 'value'}
	 */
	@Override
	public String getClientMetadata() {
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
	
		/*
		 * For our device.. we form a UUID using the laptop's wlan0 interface MAC-address.
		 */
		try {

			NetworkInterface network = NetworkInterface.getByName("wlan0");
			byte[] mac = network.getHardwareAddress();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
			}
			
			return "LINUX-DESKTOP:ETH0:MAC:" + sb.toString();

		} catch (SocketException e) {
			e.printStackTrace();
			
		}
		
		return null;
	}
}
