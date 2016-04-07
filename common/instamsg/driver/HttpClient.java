package common.instamsg.driver;

import java.util.HashMap;
import java.util.Map;

import common.instamsg.driver.InstaMsg.ReturnCode;
import device.linux.instamsg.DeviceSocket;
import device.linux.instamsg.common.FileUtils;

public class HttpClient {

	final static String contentLengthPrefix = "Content-Length:";

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
	
	private static void handleSocketError(Socket s, String log) {
		
		s.releaseSocket();
		Log.errorLog(log + " ... returning :( :(");
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
	public static HttpResponse downloadFile(String url, String downloadedFilePath, Map<String, String> params, Map<String, String> headers,
			                                int timeout) {
		
		Socket socket = new DeviceSocket("platform.instamsg.io", 80);
		socket.initSocket();;
		
		if(socket.socketCorrupted == true) {
			handleSocketError(socket, "Could not instantiate socket for downloading-file");
			return null;
		}
		
		String completeUrl = getCompleteUrl(REQUEST_TYPE.GET, url, params, headers);
		ReturnCode rc = socket.socketWrite(completeUrl.getBytes(), completeUrl.getBytes().length);
		
		if(rc == ReturnCode.FAILURE) {	
			handleSocketError(socket, "Could not send-request for downloading-file");
			return null;
		}
		
		int contentLength = 0;
		
		while(true) {
			
			String nextLine = getNextLine(socket);
			if(nextLine == null) {
				handleSocketError(socket, "Error while reading meta-response for downloading-file");
				return null;	
			}
			
			if(nextLine.startsWith(contentLengthPrefix)) {
				contentLength = Integer.parseInt(nextLine.substring(contentLengthPrefix.length()).trim());
			}
			
			if(nextLine.length() == 0) {
				Log.debugLog("Time to download the file !!!");
				break;
			}
		}
		
		String file = "";
		for(int i = 0; i < contentLength; i++) {
			byte[] b = new byte[1];
			
			if(socket.socketRead(b, 1, true) == ReturnCode.FAILURE) {
				handleSocketError(socket, "Error while downloading content of file");
				return null;	
			}
			
			String nextChar = new String(b);
			file = file + nextChar;
		}
		
		FileUtils.removeFile(downloadedFilePath);
		FileUtils.appendLine("[FILE-DOWNLOAD]", downloadedFilePath, file);
		
		socket.releaseSocket();		
		return new HttpResponse(200, "");	
	}
	
	
	public static void main(String[] args) {
		
		InstaMsg.instaMsg = new InstaMsg();
		downloadFile("/files/7bdfa2be-bbb7-4d13-b89e-c491a1b3cb83.gst_test.py", "/home/ajay/downloaded.txt",
					 new HashMap<String, String>(), new HashMap<String, String>(), 10);

	}	
}
