package device.linux.instamsg;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
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
				SocketFactory socketFactory = null;
				
				boolean certificateFilesExist = false;
				certificateFilesExist = new File(InstaMsg.CERT_KEY_FILE).exists() && new File(InstaMsg.CERT_CERT_FILE).exists();
				
				if(certificateFilesExist == true){
					
					PrivateKey privateKey = PemReader.loadPrivateKey(InstaMsg.CERT_KEY_FILE);
					X509Certificate certificate = PemReader.loadPublicX509(InstaMsg.CERT_CERT_FILE);
					
					KeyStore ks = KeyStore.getInstance("PKCS12");
					ks.load(null, null);
					ks.setKeyEntry("1", privateKey, null, new java.security.cert.Certificate[]{certificate});
					
					
					/*KeyStore ks = KeyStore.getInstance("pkcs12");
					InputStream ksIs = new FileInputStream("/home/gsachan/5e8cdaa0-fa5d-11e5-8d85-a41f726775dd.p12");
					ks.load(ksIs, "password".toCharArray());*/
					 
					KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					kmf.init(ks, null);
					 
					SSLContext sc = SSLContext.getInstance("TLSv1.2");
					
					/*
					 * Either of the first two parameters may be null in which case the installed security providers will be searched for the highest priority implementation of the appropriate factory.
					 * 
					 * So as per our use case we do not have any trust chain. thats why authentication can be done using certificate itself.
					 * 
					 * 
					 * Further read  https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html#init(javax.net.ssl.KeyManager[],%20javax.net.ssl.TrustManager[],%20java.security.SecureRandom)
					 */
					
					//TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					//trustManagerFactory.init(ks);
					sc.init(kmf.getKeyManagers(),null, new SecureRandom());
					 
					socketFactory = sc.getSocketFactory();
				}
				else{
					socketFactory = SSLSocketFactory.getDefault();
				}
				
				
				socket = socketFactory.createSocket(host, port);
			}
			else{
				socket = new java.net.Socket(host, port);
			}
			socket.setSoTimeout(InstaMsg.SOCKET_READ_TIMEOUT_SECS * 1000);
			
		} catch (Exception e) {
			Log.errorLog(SOCKET_ERROR + "Error occurred while connecting to [" + host + "] on port [" + port + "]");
			e.printStackTrace();
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
				
				if(buffer[i] == -1) {
					return InstaMsg.ReturnCode.FAILURE;
				}
				
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
