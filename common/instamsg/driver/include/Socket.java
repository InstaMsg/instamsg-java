package common.instamsg.driver.include;

import common.instamsg.driver.Globals.ReturnCode;

public abstract class Socket {

	String host;
	int port;
	
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
	}
	
	public abstract void connectUnderlyingSocketMediumTryOnce();	
	public abstract ReturnCode socketRead(byte[] buffer, int len, boolean guaranteed);
	public abstract ReturnCode socketWrite(byte[] buffer, int len);
	public abstract void releaseUnderlyingSocketMediumGuaranteed();
}
