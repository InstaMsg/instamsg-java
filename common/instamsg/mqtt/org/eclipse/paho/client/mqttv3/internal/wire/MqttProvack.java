/* 
 * Copyright (c) 2009, 2012 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */
package common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import common.instamsg.driver.Json;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttException;

/**
 * An on-the-wire representation of an MQTT CONNACK.
 */


public class MqttProvack extends MqttAck {

	public static final int PROVISIONING_SUCCESSFUL = 0;
	public static final int PROVISIONING_SUCCESSFUL_WITH_CERT = 6;

	private int returnCode;
	
	private String completePayload;
	private String clientId;
	private String secret;	
	
	private boolean isSecureSslCertificate = false;
	private String certificateKey = "";
	private String certificate = "";


	public MqttProvack(byte info, byte[] variableHeader) throws IOException {
		super(MqttWireMessage.MESSAGE_TYPE_PROVACK);
		ByteArrayInputStream bais = new ByteArrayInputStream(variableHeader);
		DataInputStream dis = new DataInputStream(bais);
		dis.readByte();
		returnCode = dis.readUnsignedByte();
		
		byte[] payload = new byte[variableHeader.length - 2];
		dis.readFully(payload);
		dis.close();
		
		this.completePayload = new String(payload);
		
		if(returnCode == PROVISIONING_SUCCESSFUL) {
			if(completePayload.length() > 0){
				this.clientId = completePayload.substring(0, 36);
			}

			if(completePayload.length() > 37) {
				this.secret = completePayload.substring(37, completePayload.length());
			}
			
		} else if (returnCode == PROVISIONING_SUCCESSFUL_WITH_CERT) {			
			this.clientId = Json.getJsonKeyValueIfPresent(completePayload, "client_id");
			this.secret = Json.getJsonKeyValueIfPresent(completePayload, "auth_token");
			
			this.isSecureSslCertificate = Boolean.parseBoolean(Json.getJsonKeyValueIfPresent(completePayload, "secure_ssl_certificate"));
			this.certificateKey = Json.getJsonKeyValueIfPresent(completePayload, "key");
			this.certificate = Json.getJsonKeyValueIfPresent(completePayload, "certificate");
		}
	}
	
	public int getReturnCode() {
		return returnCode;
	}

	protected byte[] getVariableHeader() throws MqttException {
		// Not needed, as the client never encodes a CONNACK
		return new byte[0];
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public String getSecret() {
		return secret;
	}
	
	public String getCompletePayload() {
		return completePayload;
	}
	
	public boolean isSecureSslCertificate() {
		return isSecureSslCertificate;
	}
	
	public String getCertificateKey() {
		return certificateKey;
	}
	
	public String getCertificate() {
		return certificate;
	}

	
	/**
	 * Returns whether or not this message needs to include a message ID.
	 */
	public boolean isMessageIdRequired() {
		return false;
	}
	
	public String getKey() {
		return new String("Con");
	}
	
	public String toString() {
		return super.toString() + " return code: " + returnCode;
	}
	
}
