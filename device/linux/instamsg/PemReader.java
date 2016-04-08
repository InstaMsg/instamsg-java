package device.linux.instamsg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

public class PemReader {
	public static X509Certificate loadPublicX509(String fileName) throws GeneralSecurityException, IOException {
	    
	    X509Certificate crt = null;
	    try(InputStream is = new FileInputStream(fileName)) {
	        //is = fileName.getClass().getResourceAsStream(fileName);
	    	
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        crt = (X509Certificate)cf.generateCertificate(is);
	    }
	    return crt;
	}

	public static PrivateKey loadPrivateKey(String fileName) throws IOException, GeneralSecurityException {
	    PrivateKey key = null;
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))){
	        //is = fileName.getClass().getResourceAsStream(fileName);
	        StringBuilder builder = new StringBuilder();
	        boolean inKey = false;
	        for (String line = br.readLine(); line != null; line = br.readLine()) {
	            if (!inKey) {
	                if (line.startsWith("-----BEGIN ") && 
	                        line.endsWith(" PRIVATE KEY-----")) {
	                    inKey = true;
	                }
	                continue;
	            }
	            else {
	                if (line.startsWith("-----END ") && 
	                        line.endsWith(" PRIVATE KEY-----")) {
	                    inKey = false;
	                    break;
	                }
	                builder.append(line);
	            }
	        }
	        //
	        byte[] encoded = DatatypeConverter.parseBase64Binary(builder.toString());
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        key = kf.generatePrivate(keySpec);
	    }
	    return key;
	}

	public static void closeSilent(final InputStream is) {
	    if (is == null) return;
	    try { is.close(); } catch (Exception ign) {}
	}
}
