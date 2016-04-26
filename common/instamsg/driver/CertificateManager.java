package common.instamsg.driver;

public class CertificateManager {
	
	private static String CERT_MODULE   = "[CERTIFICATE]";
	public static String CERT_KEY_FILE  = "/home/sensegrow/key";
	public static String CERT_CERT_FILE = "/home/sensegrow/cert"; 
	
	public static void processCertificateInfoIfAny(String payload) {
		
		boolean isSecureSslCertificate = Boolean.parseBoolean(Json.getJsonKeyValueIfPresent(payload, "secure_ssl_certificate"));
		if(isSecureSslCertificate == true) {
			/*
			 * For this, we assume that the file has to have a file-system.
			 * Thus, saving the certificate-file(s) has been integrated in the driver-code itself.
			 */
			FileUtils.createFileAndAddContent(CERT_MODULE, Json.getJsonKeyValueIfPresent(payload, "key").replaceAll("\\\\n", "\n"), CERT_KEY_FILE);
			FileUtils.createFileAndAddContent(CERT_MODULE, Json.getJsonKeyValueIfPresent(payload, "certificate").replaceAll("\\\\n", "\n"), CERT_CERT_FILE);
		}
	}
}
