package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

public class OneToOneResult {
	
	    public OneToOneResult(String peer, int peerMsgId, boolean succeeded, String peerMsg) {
	    	
	    	super();
	    	
	    	this.peer = peer;
	    	this.peerMsgId = peerMsgId;
	    	this.succeeded = succeeded;
	    	this.peerMsg = peerMsg;
	    }

		/*
	     ************** NOT EXPECTED TO BE USED BY THE APPLICATION ******************************
	     */
	    public String peer;
	    public int peerMsgId;


	    /*
	     ************** EXPECTED TO BE USED BY THE APPLICATION **********************************
	     */

	    /*
	     * Is one of 0, 1.
	     *
	     * 0 denotes that there was some error while fetching the response from peer.
	     * 1 denotes that the response was succesfully received.
	     */
	    public boolean succeeded;

	    /*
	     * Peer-Message.
	     *
	     * Makes sense only if the value of "succeeded" is 1.
	     */
	    public String peerMsg;

	    /*
	     * Function-Pointer, to send a reply to the peer.
	     *
	     * Kindly see
	     *
	     *                  common/apps/oneToOneInitiator/main.java
	     *                  common/apps/subscriber/main.java
	     *
	     * for simple (yet complete) example-usage.
	     */
	    public ReturnCode reply(String replyMessage) {
	    	
	        int msgId = InstaMsg.getNextPackedId(InstaMsg.instaMsg);

	        String message = "{\"message_id\": \""     +  msgId                              + 
	        		         "\", \"response_id\": \"" +  peerMsgId                          +
	        		         "\", \"reply_to\": \""    +  InstaMsg.instaMsg.clientIdComplete +
	        		         "\", \"body\": \""        +  replyMessage                       + 
	        		         "\", \"status\": 1}";

	        return InstaMsg.doMqttSendPublish(peer, message);
	    }


}