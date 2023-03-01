package au.com.blueoak.portal.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.blueoak.portal.BaseTesting;

public class CommonMethods {
	
	protected static final Log LOG = LogFactory.getLog(BaseTesting.class);
	
	public static void runCommand(String command, String logToCheck) {
		
		try {
			String path = System.getProperty("user.dir");
			String cdPath = "cd \"".concat(path).concat("\" && ").concat(command);
			ProcessBuilder procBuilder = new ProcessBuilder("cmd.exe", "/c", cdPath);
			procBuilder.redirectErrorStream(true);
			Process proc = procBuilder.start();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while (true) {
				line = br.readLine();
				if (line.toLowerCase().contains(logToCheck.toLowerCase())) {
					Thread.sleep(5000);
					if (LOG.isDebugEnabled()) {
						LOG.debug("The value of line: ".concat(line));
					}
					break;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	
}
