package common.instamsg.driver;


public class Json {

	/**
	 * This is a very restricted version of json-parsing, which involves only a python-dict.
	 *
	 * Upon every parsing-request, the complete json will be parsed.
	 * But here (in the embedded-scenario), we want correctness and reliability (speed is kinda secondary).
	 *
	 * The key/value can be without double-quotes or single-quotes, but we will return them as a string nevertheless
	 * (the calling-function will do the necessary conversions as necessary).
	 */
	public static String getJsonKeyValueIfPresent(String json, String key)
	{
		String value = "";
		
	    char NOT_FOUND = ' ';
	    char keyWrapper = NOT_FOUND;
	    
	    ChangeableString parsedKeyToken = new ChangeableString("");
	    ChangeableString parsedValueToken = new ChangeableString("");
	    ChangeableString token = parsedKeyToken;


	    for(int i = 0; i < json.length(); i++) {
	    	
	        if((keyWrapper == NOT_FOUND) && (json.charAt(i) == NOT_FOUND)) {
	
	        } else if((keyWrapper == NOT_FOUND) && ((json.charAt(i) == '{') || (json.charAt(i) == ','))) {
	        	
	        } else if((keyWrapper == NOT_FOUND) && ((json.charAt(i) == '\'') || (json.charAt(i) == '"'))) {
	        	
	            /*
	             *  This means we need to start parsing the key now. 
	             */
	            keyWrapper = json.charAt(i);
	            
	        } else if((json.charAt(i) == ':') && (keyWrapper == NOT_FOUND)) {
	        	
	        }
	        else if(    (json.charAt(i) == keyWrapper) ||
	                    ((json.charAt(i) == '}') && (keyWrapper == NOT_FOUND))) {
	        	
	            /*
	             *  We need to stop parsing the key now.
	             */
	            keyWrapper = NOT_FOUND;

	            /*
	             *  Now, if we were currrently parsing-key, move to parsing value.
	             */
	            if((parsedValueToken.toString().length() == 0) && (token == parsedKeyToken)) {
	            	
	                token = parsedValueToken;
	                
	            } else {
	            	
	                /*
	                 *  If we found the current key-value, we are done. 
	                 */
	                if(parsedKeyToken.toString().equals(key) == true)
	                {
	                	value = value + parsedValueToken;

	                	Log.debugLog("Found key [" + parsedKeyToken.toString() + "] and value [" + value + "] in json [" + json + "]");
	                    return value;
	                }

	                /*
	                 *  We have parsed current key-value pair. So, reset the token-buffers.
	                 */
	                parsedKeyToken.changeTo("");
	                parsedValueToken.changeTo("");

	                /* 
	                 * Set the current-token to "key"-parsing-mode.
	                 */
	                token = parsedKeyToken;
	            }
	        }
	        else
	        {
	            /* Simply add to the running token. */
	            token.changeTo(token.toString() + json.charAt(i));
	        }
	    }
	    
	    return value;
	}
	
	
	public static void main(String[] args) {
		
		String json = "{'ajay'  :\"garg\", \"second\": 125}";
		System.out.println(Json.getJsonKeyValueIfPresent(json, "second"));
	}
}
