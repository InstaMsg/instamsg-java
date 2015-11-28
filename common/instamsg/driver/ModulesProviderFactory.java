package common.instamsg.driver;


public class ModulesProviderFactory {
	
	public static ModulesProviderInterface getModulesProvider(String device){
		
		if(device.equals("stub")){
			return new device.stub.instamsg.ModulesProvider();
		}
		else if(device.equals("linux")){
			return new device.linux.instamsg.ModulesProvider();
		}
		/*
		 * All new device-implementors must add their devices in this factory.
		 */
		
		return null;
	}
	
}
