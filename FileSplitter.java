// /*
// 	Alexander Shmakov
// 	cmpt400:ReportB
// 	Mar 27, 2018
// */

// import java.util.*;
// import java.io.*;

// public class FileSplitter {

// 	int parts;
// 	File file;
// 	byte[] bytes;
// 	byte[][] bytesParts;
// 	FileInputStream fis;

// 	//this constructor reads the bytes of the file passed
// 	public FileSplitter(String filename, int packetSize) throws IOException {
// 		file = null;
// 		bytes = null;
// 		bytesParts = null;
// 		fis = null;

// 		try {
// 			file = new File(filename);
// 			if(!file.exists()) { 
// 				return;
// 			}
// 			fis = new FileInputStream(file);
// 			bytes = new byte[(int)file.length()]; 	
// 			this.parts = this.bytes.length / packetSize;
// 			fis.read(bytes);
// 			fis.close();
// 		}
// 		catch (Exception e) {}

// 		divideToParts(packetSize);
// 	}

// 	//this method splits the byte array to the number of parts requested
// 	private void divideToParts(int offset) {

// 		bytesParts = new byte[this.parts][];
// 		int currPos = 0;
// 		int nextPos = 0;

// 		for(int i=0; i < this.parts; i++) {
// 			nextPos += offset;
// 			if(i+1 == this.parts) {
// 				nextPos = this.bytes.length;
// 			}
// 			this.bytesParts[i] = Arrays.copyOfRange(this.bytes, currPos, nextPos);
// 			currPos += offset;
// 		}
// 	}

// 	//this method is a getter for the 2d array of bytes
// 	public byte[][] getParts() {
// 		return this.bytesParts;
// 	}
// }


/*
	Alexander Shmakov
	cmpt400:ReportB
	Mar 27, 2018
*/

import java.util.*;
import java.io.*;

public class FileSplitter {

	int parts;
	File file;
	byte[] bytes;
	byte[][] bytesParts;
	FileInputStream fis;

	//this constructor reads the bytes of the file passed
	public FileSplitter(String filename, int packetSize) throws IOException {
		file = null;
		bytes = null;
		bytesParts = null;
		fis = null;

		try {
			file = new File(filename);
			if(!file.exists()) { 
				return;
			}
			fis = new FileInputStream(file);
			bytes = new byte[(int)file.length()];
			
			this.parts = (this.bytes.length > packetSize) ? (this.bytes.length/packetSize)+1 : 1;
			System.out.println("SERVER SPLITTED TO peaces: "+this.parts);
			
			fis.read(bytes);
			fis.close();
		}
		catch (Exception e) {}

		divideToParts(packetSize);
	}

	//this method splits the byte array to the number of parts requested
	private void divideToParts(int offset) {

		bytesParts = new byte[this.parts][];
		int currPos = 0;
		int nextPos = 0;

		for(int i=0; i < this.parts; i++) {
			nextPos += offset;
			if(i+1 == this.parts) {
				nextPos = this.bytes.length;
			}
			this.bytesParts[i] = Arrays.copyOfRange(this.bytes, currPos, nextPos);
			currPos += offset;
		}
	}

	//this method is a getter for the 2d array of bytes
	public byte[][] getParts() {
		return this.bytesParts;
	}
}
