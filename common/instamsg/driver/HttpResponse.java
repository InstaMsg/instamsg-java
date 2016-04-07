package common.instamsg.driver;

public class HttpResponse {

	int status;
	String body;
	
	public HttpResponse(int status, String body) {
		super();
		
		this.status = status;
		this.body = body;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getBody() {
		return body;
	}
}
