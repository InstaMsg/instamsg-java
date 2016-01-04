package common.instamsg.driver;

public interface Media {

	void createAndStartStreamingPipeline(String mediaServerIpAddress, String mediaServerPort);
	void pauseStreaming();
	void stopStreaming();
}
