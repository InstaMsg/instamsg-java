package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;


public abstract class Socket {

	public static final String SOCKET_ERROR = "[SOCKET-ERROR] ";
	
	protected String host;
	protected int port;
	
	public boolean socketCorrupted = true;
	
	protected Socket(String hostName, int port) {
		
		this.host = hostName;
		this.port = port;
	}
	
	public void initSocket() {
		
		connectUnderlyingSocketMediumTryOnce();
	}

	public void releaseSocket() {
		
		releaseUnderlyingSocketMediumGuaranteed();		
	    Log.infoLog("COMPLETE [TCP-SOCKET] STRUCTURE, INCLUDING THE UNDERLYING MEDIUM CLEANED FOR HOST = [" +
	                     InstaMsg.INSTAMSG_HOST + "], PORT = [" + InstaMsg.INSTAMSG_PORT + "].");

	}
	
	public abstract void connectUnderlyingSocketMediumTryOnce();	
	public abstract InstaMsg.ReturnCode socketRead(byte[] buffer, int len, boolean guaranteed);
	public abstract InstaMsg.ReturnCode socketWrite(byte[] buffer, int len);
	public abstract void releaseUnderlyingSocketMediumGuaranteed();
}
