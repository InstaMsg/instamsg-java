package common.instamsg.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import common.instamsg.driver.InstaMsg.ReturnCode;
import config.DeviceConstants;

public class HttpClient {

	private static final String FILE_DOWNLOAD_SUCCESS_MESSAGE = "";
	private static final String ERROR_READING_FILE_CONTENT = "Error while downloading content of file";
	private static final String ERROR_READING_META_RESPONSE = "Error while reading meta-response for downloading-file";
	private static final String REQUEST_NOT_SEND = "Could not send-request for downloading-file";
	private static final String SOCKET_NOT_AVAILABLE = "Could not instantiate socket for downloading-file";
	private static final String FILE_NOT_COPYABLE = "Could not copy content to the end-device";

	final static String contentLengthPrefix = "Content-Length:";	
	static String FILE_DOWNLOAD             = "[FILE-DOWNLOAD] ";

	final static int DOWNLOAD_FILE_SUCCESS = 200;
	final static int DOWNLOAD_FILE_FAILURE = 404;

	enum REQUEST_TYPE {
		GET
	}
	
	private static String getNextLine(Socket s) {
		String line = "";
		
		while(true) {
			byte[] b = new byte[1];
			
			if(s.socketRead(b, 1, true) == ReturnCode.FAILURE) {
				return null;
			}
			
			String nextChar = new String(b);
			
			if(nextChar.equals("\n")) {
				return line;
			} else if(nextChar.equals("\r") == false) {
				line = line + nextChar;
			}			
		}
	}
	
	
	private static String getCompleteUrl(REQUEST_TYPE requestType, String url, Map<String, String> params, Map<String, String> headers) {
		String completeUrl = "";
		
		completeUrl = completeUrl + requestType.toString() + " " + url;
		
		int i = 0;
		for(String key : params.keySet()) {
			if(i == 0) {
				completeUrl = completeUrl + "?";
			} else {
				completeUrl = completeUrl + "&";
			}
			
			completeUrl = completeUrl + key + "=" + params.get(key);
			
			i++;
		}
	
		completeUrl = completeUrl + " HTTP/1.0\r\n";
		
		for(String key : headers.keySet()) {
			completeUrl = completeUrl + key + ":" + params.get(key) + "\r\n";
		}
		
		completeUrl = completeUrl + "\r\n";
		return completeUrl;
	}
	
	private static void handleSocketError(Socket s) {
		
		s.releaseSocket();
	}
	
	
	/**
	 * Either of the URLs form work ::
	 *
	 *      http://platform.instamsg.io:8081/files/d2f9d9e7-e98b-4777-989e-605073a55efd.0003-Missed-a-path-export.patch
	 *      /files/d2f9d9e7-e98b-4777-989e-605073a55efd.0003-Missed-a-path-export.patch
	 */

	/**
	 * BYTE-LEVEL-REQUEST ::
	 * ======================
	 *
	 * GET /files/d2f9d9e7-e98b-4777-989e-605073a55efd.0003-Missed-a-path-export.patch HTTP/1.0\r\n\r\n
	 *
	 *
	 * BYTE-LEVEL-RESPONSE ::
	 * =======================
	 *
	 * HTTP/1.1 200 OK
	 * Date: Wed, 05 Aug 2015 09:43:26 GMT
	 * Server: Apache/2.4.7 (Ubuntu)
	 * Last-Modified: Wed, 05 Aug 2015 09:14:51 GMT
	 * ETag: "f-51c8cd5d313d7"
	 * Accept-Ranges: bytes
	 * Content-Length: 15
	 * Connection: close
	 * Content-Type: text/plain
	 *
	 * echo "hi ajay"
	 */
	public static HttpResponse downloadFile(InstaMsg im, String url, String downloadedFilePath,
			                                Map<String, String> params, Map<String, String> headers, int timeout) {
		
		int portToUse = 0;
		
		if(DeviceConstants.SSL_SOCKET == true) {
			portToUse = DeviceConstants.INSTAMSG_HTTPS_PORT;
		} else {
			portToUse = DeviceConstants.INSTAMSG_HTTP_PORT;
		}
		
		Socket socket = im.modulesProvideInterface.getSocket(DeviceConstants.INSTAMSG_HTTP_HOST, portToUse);
		socket.initSocket();
		
		if(socket.socketCorrupted == true) {
			handleSocketError(socket);
			return new HttpResponse(DOWNLOAD_FILE_FAILURE, SOCKET_NOT_AVAILABLE);
		}
		
		String completeUrl = getCompleteUrl(REQUEST_TYPE.GET, url, params, headers);
		ReturnCode rc = socket.socketWrite(completeUrl.getBytes(), completeUrl.getBytes().length);
		
		if(rc == ReturnCode.FAILURE) {	
			handleSocketError(socket);
			return new HttpResponse(DOWNLOAD_FILE_FAILURE, REQUEST_NOT_SEND);
		}
		
		int contentLength = 0;
		
		while(true) {
			
			String nextLine = getNextLine(socket);
			if(nextLine == null) {
				handleSocketError(socket);
				return new HttpResponse(DOWNLOAD_FILE_FAILURE, ERROR_READING_META_RESPONSE);	
			}
			
			if(nextLine.startsWith(contentLengthPrefix)) {
				contentLength = Integer.parseInt(nextLine.substring(contentLengthPrefix.length()).trim());
			}
			
			if(nextLine.length() == 0) {
				Log.debugLog("Time to download the file !!!");
				break;
			}
		}
		
		FileUtils.removeFile(downloadedFilePath);
		
		int i = 0;
		try (FileOutputStream os = new FileOutputStream(new File(downloadedFilePath))) {
			for(i = 0; i < contentLength; i++) {
				byte[] b = new byte[1];
				
				if(socket.socketRead(b, 1, true) == ReturnCode.FAILURE) {
					handleSocketError(socket);
					return new HttpResponse(DOWNLOAD_FILE_FAILURE, ERROR_READING_FILE_CONTENT);	
				}
				
				if((i % DeviceConstants.OTA_PING_BUFFER_SIZE) == 0) {
					Log.infoLog(i + " / " + contentLength + " bytes downloaded ...");
					
					InstaMsg.sendPingReqToServer(im);
					InstaMsg.readAndProcessIncomingMQTTPacketsIfAny(im);
				}
				os.write(b);
			}
			
		} catch (Exception e) {
			
			handleSocketError(socket);
			return new HttpResponse(DOWNLOAD_FILE_FAILURE, FILE_NOT_COPYABLE);
		}		
		
		Log.infoLog(i + " / " + contentLength + " bytes downloaded ...");
		Log.infoLog(FILE_DOWNLOAD + "File-Download SUCCESS !!!!!!!!!!");
		
		socket.releaseSocket();		
		return new HttpResponse(DOWNLOAD_FILE_SUCCESS, FILE_DOWNLOAD_SUCCESS_MESSAGE);	
	}
	
	
	public static void main(String[] args) {
		
		InstaMsg.instaMsg = new InstaMsg();
		downloadFile(InstaMsg.instaMsg, "/files/7bdfa2be-bbb7-4d13-b89e-c491a1b3cb83.gst_test.py", "/home/ajay/downloaded.txt",
					 new HashMap<String, String>(), new HashMap<String, String>(), 10);

	}	
}
