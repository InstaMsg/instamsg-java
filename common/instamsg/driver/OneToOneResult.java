package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

public class OneToOneResult {
	
	    public OneToOneResult(String peer, int peerMsgId, boolean succeeded, String peerMsg) {
	    	
	    	super();
	    	
	    	this.peerClientId = peer;
	    	this.peerMsgId = peerMsgId;
	    	this.succeeded = succeeded;
	    	this.peerMsg = peerMsg;
	    }
	    

	    
	    
	    
	    
	    

		/*
	     ************** NOT EXPECTED TO BE USED BY THE APPLICATION ******************************
	     */
	    public String peerClientId;
	    public int peerMsgId;
	    


	    /*
	     ************** EXPECTED TO BE USED BY THE APPLICATION **********************************
	     */

	    /*
	     * Is one of "false", "true".
	     *
	     * "false" denotes that there was some error while fetching the response from peer.
	     * "true" denotes that the response was succesfully received.
	     */
	    public boolean succeeded;
	    
	    

	    /*
	     * Peer-Message.
	     *
	     * Makes sense only if the value of "succeeded" is "true".
	     */
	    public String peerMsg;
	    
	    

	    /*
	     * Function-Pointer, to send a reply to the peer.
	     *
	     * Kindly see
	     *
	     *                  common/apps/oneToOneInitiator/Main.java
	     *                  common/apps/subscriber/Main.java
	     *
	     * for simple (yet complete) example-usage.
	     */
	    public ReturnCode reply(String msg,
	    		                OneToOneHandler replyHandler,
	    		                int replyHandlerTimeout) {
	    	
	        int msgId = InstaMsg.getNextPackedId(InstaMsg.instaMsg);

	        String message = "{\"message_id\": \""     +  msgId                              + 
	        		         "\", \"response_id\": \"" +  peerMsgId                          +
	        		         "\", \"reply_to\": \""    +  InstaMsg.instaMsg.clientIdComplete +
	        		         "\", \"body\": \""        +  msg                       	 + 
	        		         "\", \"status\": 1}";

	        return InstaMsg.doMqttSendPublish(msgId, replyHandler, replyHandlerTimeout, peerClientId, message);
	    }
}