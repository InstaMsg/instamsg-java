package device.linux.instamsg;

import common.instamsg.driver.Media;

public class DeviceMedia implements Media {

	/**
	 * This method starts streaming, to the desired media-server.
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
