package device.linux.instamsg;


import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.Log;

public class DeviceLogger extends Log {



	/**
	 * This method MUST connect the underlying medium (even if it means to retry continuously).
	 */
	@Override
	public void initLogger() {
		
	}

	
	/**
	 * This method writes first "len" bytes from "buffer" onto the serial-logger-interface.
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
	 * In this case, FAILURE must be returned immediately.
	 */
	@Override
	public InstaMsg.ReturnCode loggerWrite(byte[] buffer, int len) {
		
		System.out.println(new String(buffer));
		return InstaMsg.ReturnCode.SUCCESS;
	}

	
	/**
	 * This method MUST release the underlying medium (even if it means to retry continuously).
	 * But if it is ok to re-connect without releasing the underlying-system-resource, then this can be left empty.
	 */
	@Override
	public void releaseLogger() {
		
	}

}
