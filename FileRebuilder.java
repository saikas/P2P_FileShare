/*
	Alexander Shmakov
	cmpt400:Project
	Apr 8, 2018
*/

import java.util.*;
import java.io.*;

public class FileRebuilder {
	
	File file;
	FileOutputStream fos;
	byte[] bytes;

	public FileRebuilder(byte[][] byteParts, String name) throws IOException {

		String[] tmp = name.split("/");
		String newName = tmp[tmp.length-1];
		if(new File("files", newName).exists()) {
			newName = "files/copy-"+newName;
		}
		else {
			newName = "files/"+newName;
		}

		try {
			bytes = combineBytes(byteParts);
			file = new File(newName);
			fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();
		}
		catch(Exception e) {}
	}

	private byte[] combineBytes(byte[][] byteParts) {
		byte[] bytes;
		int arrSize = 0;
		for (int i=0; i<byteParts.length; i++) {
			arrSize += byteParts[i].length;
		}

		bytes = new byte[arrSize];
		int idx = 0;
		for (int j=0; j<byteParts.length; j++) {
			for (int n=0; n<byteParts[j].length; n++) {
				bytes[idx] = byteParts[j][n];
				idx++;
			}
		}
		return bytes;
	}
}