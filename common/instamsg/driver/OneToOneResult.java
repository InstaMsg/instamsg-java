package common.instamsg.driver;

public class OneToOneResult {
	    /*
	     ************** NOT EXPECTED TO BE USED BY THE APPLICATION ******************************
	     */
	    String peer;
	    int peerMsgId;


	    /*
	     ************** EXPECTED TO BE USED BY THE APPLICATION **********************************
	     */

	    /*
	     * Is one of 0, 1.
	     *
	     * 0 denotes that there was some error while fetching the response from peer.
	     * 1 denotes that the response was succesfully received.
	     */
	    boolean succeeded;

	    /*
	     * Peer-Message.
	     *
	     * Makes sense only if the value of "succeeded" is 1.
	     */
	    String peerMsg;

	    /*
	     * Function-Pointer, to send a reply to the peer.
	     *
	     * Kindly see
	     *
	     *                  common/apps/one_to_one_initiator/main.c
	     *                  common/apps/subscriber/main.c
	     *
	     * for simple (yet complete) example-usage.
	     */
	    void reply(String replyMessage){
	    	//TODO: add implementation
	    }
}