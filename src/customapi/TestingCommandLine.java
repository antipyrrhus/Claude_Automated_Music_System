package customapi;

import java.io.File;
import java.io.IOException;

public class TestingCommandLine {

	public static void main(String[] args) {
		Runtime rt = Runtime.getRuntime();
		try {
			Process pr = rt.exec("cmd /c javac 'D:/Dropbox/Dropbox/-=EDU/GATech-MS route/2018 Spring/8903 - Special Topics/Claude/src/customapi/CustomFunctions.java'");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
