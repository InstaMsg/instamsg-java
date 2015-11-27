package device.linux.instamsg;

import java.net.NetworkInterface;
import java.net.SocketException;

import common.instamsg.driver.Misc;

public class DeviceMisc implements Misc {

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
