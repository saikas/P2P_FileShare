/*
	Alexander Shmakov
	cmpt400:Project
	Apr 8, 2018
*/

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FileShare {

	public static void main(String[] args) throws IOException {

		int numParts = 50;
		int timeout = 3000;
		double dropProbability = 0.3;
		String fileList = "", hostList = "";

		try {
			if(args.length == 2) {	
				fileList = args[0];
				hostList = args[1];
			}
			else {
				System.out.println("USAGE: java FileShare <FILENAME> <FILENAME>");
				System.exit(1);
			}
		} catch(Exception e) {}


		// String[] clientArgs = {fileList, hostList, ""+(int)( (timeout/numParts)<0 ? 10 : (timeout/numParts))};
		String[] clientArgs = {fileList, hostList, ""+timeout};
		String[] serverArgs = {fileList, ""+numParts, ""+timeout};

		Thread server = new Thread(new Runnable() {
			public void run() {
				try{Server.main(serverArgs);}
				catch(IOException ioe) {}
			}
		});

		Thread client = new Thread(new Runnable() {
			public void run() {
				try {Client.main(clientArgs);}
				catch(IOException ioe) {}	
			}
		});

		server.start();
		client.start();
	}//main
}
