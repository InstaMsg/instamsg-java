package device.stub.instamsg;

import common.instamsg.driver.Media;

public class DeviceMedia implements Media {

	/**
	 * This method starts streaming, to the desired media-server.
	 * THIS MUST BE DONE IN A DEDICATED THREAD.
	 * 
	 * If an error occurs while streaming, the variable
	 *            InstaMsg.mediaStreamingErrorOccurred
	 * must be set equal to "true".
	 * 
	 */
	@Override
	public void createAndStartStreamingPipeline(String mediaServerIpAddress, String mediaServerPort) {
	}


	/**
	 * This method pauses the streaming.
	 */
	@Override
	public void pauseStreaming() {
	}


	/**
	 * This method stops the streaming.
	 */
	@Override
	public void stopStreaming() {
	}
}
