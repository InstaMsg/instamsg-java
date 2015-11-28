package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

import config.DeviceConstants;



public abstract class Socket {

	public static final String SOCKET_ERROR = "[SOCKET-ERROR] ";
	
	protected String host;
	protected int port;
	
	
	/**
	 * These three variables will be parsed for GSM-devices from the "common" instamsg-code.
	 * Thereafter, it is upto the device-implementors on how to use these variables in their respective socket-implementations.
	 */
	protected String gsmApn;
	protected String gsmUser;
	protected String gsmPass;
	
	
	public boolean socketCorrupted = true;
	
	protected Socket(String hostName, int port) {
		
		this.host = hostName;
		this.port = port;
	}
	
	@SuppressWarnings("unused")
	public void initSocket() {

		if(DeviceConstants.GSM_DEVICE == true) {

			/*
			 *  Fill-in the provisioning-parameters from the SMS obtained from InstaMsg-Server 
			 */
			String sms = null;
			while((sms == null) || (sms.length() == 0)) {

				Log.infoLog("\n\n\nProvisioning-SMS not available, retrying to fetch from storage area\n\n\n");
				InstaMsg.startAndCountdownTimer(5, true);

				sms = InstaMsg.instaMsg.socket.getLatestSmsContainingSubstring("\"sg_apn\":\"");
			}

			/*
			 * For some SIMs, the "{" and "}" sent from server are replaced by "(" and ")".
			 * Rectify them.
			 */
			byte[] bytes = sms.getBytes();

			for(int i = 0; i < bytes.length; i++) {

				if(bytes[i] == '(') {
					bytes[i] = '{';
					break;
				}
			}

			for(int i = (bytes.length - 1); i >= 0; i--) {

				if(bytes[i] == ')') {
					bytes[i] = '}';
					break;
				}
			}

			sms = new String(bytes);

			gsmApn = Json.getJsonKeyValueIfPresent(sms, "sg_apn");
			gsmUser = Json.getJsonKeyValueIfPresent(sms, "sg_user");
			gsmPass = Json.getJsonKeyValueIfPresent(sms, "sg_pass");

			Log.infoLog("\nProvisioning-Params ::  sg_apn : [" + gsmApn + "], sg_user : [" + gsmUser + "], sg_pass : [" + gsmPass + "]\n");
			InstaMsg.startAndCountdownTimer(3, false);
		}
		
		
		connectUnderlyingSocketMediumTryOnce();
	}

	public void releaseSocket() {
		
		releaseUnderlyingSocketMediumGuaranteed();		
	    Log.infoLog("COMPLETE [TCP-SOCKET] STRUCTURE, INCLUDING THE UNDERLYING MEDIUM CLEANED FOR HOST = [" +
	                     InstaMsg.INSTAMSG_HOST + "], PORT = [" + InstaMsg.INSTAMSG_PORT + "].");

	}
	
	public abstract String getLatestSmsContainingSubstring(String substring);
	public abstract void connectUnderlyingSocketMediumTryOnce();	
	public abstract ReturnCode socketRead(byte[] buffer, int len, boolean guaranteed);
	public abstract ReturnCode socketWrite(byte[] buffer, int len);
	public abstract void releaseUnderlyingSocketMediumGuaranteed();
}
