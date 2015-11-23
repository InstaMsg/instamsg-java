package common.instamsg.driver.include;

public class ModulesProviderFactory {
	
	public static ModulesProviderInterface getModulesProvider(String device){
		
		if(device.equals("stub")){
			return new device.stub.instamsg.ModulesProvider();
		}
		else if(device.equals("linux")){
			return new device.linux.instamsg.ModulesProvider();
		}
		
		return null;
	}
	
}
