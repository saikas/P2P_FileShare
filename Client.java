/*
	Alexander Shmakov
	cmpt400:Project
	Apr 8, 2018
*/

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.*;
import java.nio.*;

public class Client {

	static Window win = null;
	static int numParts = 0;
	static byte[][] parts = null;
	static int timeout = 100;
	public static void main(String[] args) throws IOException {
		
		Window win = null;
		DatagramSocket sok = null;

		InetAddress serverIPAddress;
		byte[] outData = null;
		byte[] inData = null;
		String userInput = null;
		String ip = "";
		BufferedReader reader = null;
		DatagramPacket outPacket = null;
		DatagramPacket inPacket = null;

		if(args.length > 0) {
			try {
				timeout = Integer.parseInt(args[2]);
			}
			catch(Exception e) {
				System.out.println("USAGE: java Client <FILENAME> <PROTOCOL 0,1,2> <TIMEOUT>");
				System.exit(1);
			}
		}
		win = new Window();
	
		while(true) {

			outData = new byte[1400];
			inData = new byte[50000];
			sok = new DatagramSocket();

			reader = new BufferedReader(new FileReader(new File(args[1])));
			
			//GUI prompt
			while( (userInput = win.getFileName()) == null) {
				try{Thread.sleep(10);}
				catch(InterruptedException e){}
			}
			// long startTime = System.currentTimeMillis();
			win.setFileName(null);

			boolean next = true;
			//connecting to server
			while ( (ip = reader.readLine()) != null ) {
				next = true;
				System.out.println("Connecting to: "+ip);
				win.addLine("Connecting to: "+ip);
				serverIPAddress = InetAddress.getByName(ip);

				//send
				outData = userInput.getBytes();
				outPacket = new DatagramPacket(outData, outData.length, serverIPAddress, 2000);
				sok.send(outPacket);

				boolean ack = false, done = false;
				inPacket = new DatagramPacket(inData, inData.length);
				
				int count = 0;
				do {
					if(count == 2) break;

					if(numParts!=0 && isComplete(parts) == numParts) {
						done = true;
						break;
					}

					ack = recievingPackets(inPacket, sok, win);

					if(ack) {
						acknowledgment(sok, inPacket.getAddress(), inPacket.getPort(), genNegativeHeader(), win);	
						next = false;
					}
					else {count++;}

				} while(!done);

				if(next)
					win.addLine("\tTimeout: Server is down.");

				if(done) break;
			}//while ip

			if(numParts>0 && isComplete(parts) == numParts) {
				new FileRebuilder(parts, userInput);
				System.out.println("File recieved");
				win.addLine("File recieved");
				// startTime = System.currentTimeMillis()-startTime;
				// System.out.println(""+startTime);
			}
		
			ip = "";
			userInput=null;
			reader = null;
			outPacket = null;
			inPacket = null;
			outData = null;
			inData = null;
			parts = null;
			numParts = 0;
			sok.close();
		}
	}

	private static boolean recievingPackets(DatagramPacket inPacket, DatagramSocket sok, Window win) throws IOException {

		sok.setSoTimeout(timeout);
		try {
			sok.receive(inPacket);
		} 
		catch(SocketTimeoutException ste) {}
		finally {
			if(inPacket.getData().length == 0)
				return false;
			if(numParts == 0) {
				try {
					parts = new byte[readHeader(trim(inPacket.getData()), 1)][];
				} catch(IndexOutOfBoundsException e) {
					return false;
				}
				numParts = readHeader(trim(inPacket.getData()), 1);
			}
			int partNum = readHeader(trim(inPacket.getData()), 0) - 1;

			if(parts[partNum] == null) {
				String tmp = "Recieved packet: (" +(partNum+1)+ "/" +numParts+ ")";
				System.out.println(tmp);
				win.addLine(tmp);
				parts[partNum] = getPayload(trim(inPacket.getData()) );
			}

			// if(isComplete(parts) != numParts) return false;
		}
		return true;
	}

	private static byte[] genNegativeHeader() {
		ByteBuffer buff;
		byte[] header = new byte[0];
		int empty = 0;
		for (int i=0; i<parts.length; i++) {
			if(parts[i] == null && empty<4) {
				empty++;
				buff = ByteBuffer.allocate(4);
				if(empty==3 || parts.length<3) {
					//put ack byte in the first position
					header = concatBytes(buff.putInt(i).array(), header);
					break;
				}
				header = concatBytes(header ,buff.putInt(i).array());
			}//if
		}//for i

		if(empty==0) {
			buff = ByteBuffer.allocate(4);
			header = concatBytes(buff.putInt(parts.length).array(), header);
		}
		return header;
	}

	private static byte[] getByteArr(int input) {
		ByteBuffer buff = ByteBuffer.allocate(4);
		return buff.putInt(input).array();
	}

	private static void acknowledgment(DatagramSocket sok, InetAddress serverIPAddress, int port, byte[] outData, Window win) throws IOException {
		DatagramPacket outPacket = new DatagramPacket(outData, outData.length, serverIPAddress, port);
		sok.send(outPacket);
		String tmp = "\tSent acknowledgment! ID: " +readHeader(outData, 0);
		// System.out.println(tmp);
		win.addLine(tmp);
	}

	private static byte[] trim(byte[] bytes) {
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0)
			i--;
		return Arrays.copyOf(bytes, i + 1);
	}

	private static int isComplete(byte[][] parts) {
		for (int i=0; i < parts.length; i++) {
			if(parts[i] == null) {
				return i;
			}
		}
		return parts.length;
	}

	private static byte[] getPayload(byte[] bytes) {
		return Arrays.copyOfRange(bytes, 8, bytes.length);
	}

	private static int readHeader(byte[] bytes, int offset) {
		int start = (offset == 0) ? 0 : 4;
		int end = (offset == 0) ? 4 : 8;
		byte[] tmp = Arrays.copyOfRange(bytes, start, end);
		return ByteBuffer.wrap(tmp).getInt();
	}

	private static byte[] concatBytes(byte[] a, byte[] b) {
		byte[] tmp = new byte[a.length + b.length];
		for (int i=0; i<tmp.length; i++) {
			tmp[i] = (i < a.length) ? a[i] : b[i-a.length];
		}
		return tmp;
	}
}