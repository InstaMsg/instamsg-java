package device.linux.instamsg;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.InstaMsg.ReturnCode;
import common.instamsg.driver.Log;
import common.instamsg.driver.Socket;
import config.DeviceConstants;

public class DeviceSocket extends Socket {

	java.net.Socket socket = null;
	
	public DeviceSocket(String hostName, int port) {
		super(hostName, port);
	}
	
	/**
	 * This method returns the *****LATEST****** sms, which contains the desired substring.
	 *
	 * Note that "{" are sometimes not processed correctly by some SIMs, so a prefix-match (which
	 * otherwise is a stronger check) is not being done.
	 *
	 * Please note that this method is called by Instamsg-application, *****BEFORE***** calling
	 * "connect_underlying_socket_medium_try_once".
	 */
	@Override
	public String getLatestSmsContainingSubstring(String substring) {
		return null;
	}
	
	

	/**
	 * This method tries to establish the socket to super.host on super.port.
	 *
	 * If the connection is successful, then the following must be done by the device-implementation ::
	 *                          socketCorrupted = false;
	 *
	 * Setting the above value will let InstaMsg know that the connection can be used fine for writing/reading.
	 */
	@Override
	public void connectUnderlyingSocketMediumTryOnce() {
		
		try {
			if(DeviceConstants.SSL_SOCKET == true){
				SocketFactory socketFactory = SSLSocketFactory.getDefault();
				socket = socketFactory.createSocket(host, port);
			}
			else{
				socket = new java.net.Socket(host, port);
			}
			socket.setSoTimeout(InstaMsg.SOCKET_READ_TIMEOUT_SECS * 1000);
			
		} catch (Exception e) {
			
			Log.errorLog(SOCKET_ERROR + "Error occurred while connecting to [" + host + "] on port [" + port + "]");
			return;
		}
		
		socketCorrupted = false;
		Log.infoLog("TCP-SOCKET UNDERLYING_MEDIUM INITIATED FOR HOST = [" + 
		                  host + "], PORT = [" + port + "].");
	}

	/**
	 * This method reads "len" bytes from socket into "buffer".
	 *
	 * Exactly one of the cases must hold ::
	 *
	 * a)
	 * "guaranteed" is true.
	 * So, this "read" must bahave as a blocking-read.
	 *
	 * Also, exactly "len" bytes are read successfully.
	 * So, SUCCESS must be returned.
	 *
	 *                      OR
	 *
	 * b)
	 * "guaranteed" is true.
	 * So, this "read" must bahave as a blocking-read.
	 *
	 * However, an error occurs while reading.
	 * So, FAILURE must be returned immediately (i.e. no socket-reinstantiation must be done in this method).
	 *
	 *                      OR
	 *
	 * c)
	 * "guaranteed" is false.
	 * So, this "read" must behave as a non-blocking read.
	 *
	 * Also, no bytes could be read in SOCKET_READ_TIMEOUT_SECS seconds (defined in "globals.h").
	 * So, SOCKET_READ_TIMEOUT must be returned immediately.
	 *
	 *                      OR
	 *
	 * d)
	 * "guaranteed" is false.
	 * So, this "read" must behave as a non-blocking read.
	 *
	 * Also, exactly "len" bytes are successfully read.
	 * So, SUCCESS must be returned.
	 *
	 *                      OR
	 *
	 * e)
	 * "guaranteed" is false.
	 * So, this "read" must behave as a non-blocking read.
	 *
	 * However, an error occurs while reading.
	 * So, FAILURE must be returned immediately (i.e. no socket-reinstantiation must be done in this method).
	 */
	@Override
	public ReturnCode socketRead(byte[] buffer, int len, boolean guaranteed) {
		
		for(int i = 0; i < len; i++) {
			
			try {
				byte c = (byte) socket.getInputStream().read();
				buffer[i] = c;
				
			} catch (SocketTimeoutException e) {
				
				if(guaranteed == true) {
					
					/*
					 * We need to persevere till all the bytes are read.
					 */
					continue;
					
				} else {
					
					/*
					 * Case c).
					 */
					return InstaMsg.ReturnCode.SOCKET_READ_TIMEOUT;
				}
				
			} catch (IOException e) {
				
				/*
				 * Case b) and e).
				 */
				return InstaMsg.ReturnCode.FAILURE;
			}
		}

		/*
		 * Case a) and d).
		 */
		return InstaMsg.ReturnCode.SUCCESS;
	}

	
	/**
	 * This method writes first "len" bytes from "buffer" onto the socket.
	 *
	 * This is a blocking function. So, either of the following must hold true ::
	 *
	 * a)
	 * All "len" bytes are written.
	 * In this case, SUCCESS must be returned.
	 *
	 *                      OR
	 * b)
	 * An error occurred while writing.
	 * In this case, FAILURE must be returned immediately (i.e. no socket-reinstantiation must be done in this method).
	 */
	@Override
	public ReturnCode socketWrite(byte[] buffer, int len) {
		
		try {
			socket.getOutputStream().write(buffer);
			
		} catch (IOException e) {
			
			Log.errorLog(SOCKET_ERROR + "Error occurred while writing bytes to socket");
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		return InstaMsg.ReturnCode.SUCCESS;
	}

	
	/**
	 * This method does the cleaning up (for eg. closing a socket) when the socket is cleaned up.
	 * But if it is ok to re-connect without releasing the underlying-system-resource, then this can be left empty.
	 *
	 * Note that this method MUST DO """ONLY""" per-socket level cleanup, NO GLOBAL-LEVEL CLEANING/REINIT MUST BE DONE.
	 */
	@Override
	public void releaseUnderlyingSocketMediumGuaranteed() {
		
		try {
			if(socket != null) {
				socket.close();
			}
			
		} catch (IOException e) {
	
			Log.errorLog(SOCKET_ERROR + "Error occurred while closing the socket");
		}
		
	}


}
