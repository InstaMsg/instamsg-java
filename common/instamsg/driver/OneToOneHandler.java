package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

public interface OneToOneHandler {
	
	/**
	 * 
	 * This method is called, whenever a one-to-one message is received from a peer.
	 * 
	 * Multiple-peers can be handled, by putting peer-client-id specific logic in this method.
	 * The peer-client-id is available as one of the parameters in "result".
	 */
	ReturnCode oneToOneMessageHandler(OneToOneResult result);

}
