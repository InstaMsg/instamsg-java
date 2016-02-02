package device.stub.instamsg;

import common.instamsg.driver.DataLogger;



public class DeviceDataLogger extends DataLogger {	
	
	/**
	 * This method initializes the data-logger-interface for the device.
	 */
	public void initDataLogger()
	{
	}


	/**
	 * This method saves the record on the device.
	 *
	 * If and when the device-storage becomes full, the device MUST delete the oldest record, and instead replace
	 * it with the current record. That way, we will maintain a rolling-data-logger.
	 */
	public void saveRecordToPersistentStorage(String record)
	{
	}


	/**
	 * The method returns the next available record.
	 * If a record is available, following must be done ::
	 *
	 * 1)
	 * The record must be deleted from the storage-medium (so as not to duplicate-fetch this record later).
	 *
	 * 2)
	 * Then actually return the record.
	 *
	 * Obviously, there is a rare chance that step 1) is finished, but step 2) could not run to completion.
	 * That would result in a data-loss, but we are ok with it, because we don't want to send duplicate-records to InstaMsg-Server.
	 *
	 * We could have done step 2) first and then step 1), but in that scenario, we could have landed in a scenario where step 2)
	 * was done but step 1) could not be completed. That could have caused duplicate-data on InstaMsg-Server, but we don't want
	 * that.
	 *
	 *
	 * One of the following statuses must be returned ::
	 *
	 * a)
	 * SUCCESS, if a record is successfully returned.
	 *
	 * b)
	 * FAILURE, if no record is available.
	 */
	public String getNextRecordFromPersistentStorage()
	{
		return null;
	}
}
