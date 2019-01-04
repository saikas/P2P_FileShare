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

public class Server {

	private static int[] negativeAckArr = null;
	private static int successfulySent = 0; //the offset to the range of packets TCP needs to resend
	private static int timeout = 1000; //THE TIME THE SERVER WAITS BEFORE RESENDING A PACKET

	public static void main(String[] args) throws IOException {
		//default parameters
		// int numParts = 10; //THE NUMBER OF PACKETS
		double dropProb = 0.3; //DROP BROBABILITY
		int protocol = 0;
		final int OUT_PACKET_SIZE = 50000;

		if(args.length > 0) {
			try {
				// numParts = Integer.parseInt(args[1]);
				timeout = Integer.parseInt(args[2]);
			}
			catch(Exception e) {
				System.out.println("USAGE: java Server <NUMBER OF PACKETS> <TIMEOUT> <PROP PROBABILITY> <PROTOCOL>");
				System.exit(1);
			}
		}

		DatagramSocket serverSocket = null;
		byte[] inData = new byte[1400];


		int inPacketLength;
		String inputLine;

		System.out.println("Server: Waiting for file request");

		while (true) {
			try {
				serverSocket = new DatagramSocket(2000);
			} catch (IOException e) {
				System.out.println("Server: Could not listen on port: 2000\n" + e);
				System.exit(-1);
			}
	
			//get
			DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
			serverSocket.receive(inPacket);

			inPacketLength = inPacket.getLength();
			inputLine = new String(inPacket.getData(), 0, inPacketLength);


			FileSplitter splitter = new FileSplitter("files/"+inputLine, OUT_PACKET_SIZE);
			byte[][] parts = splitter.getParts();

			if(parts == null) {
				System.out.println("Server: File does not exist");
				serverSocket.close();
				continue;
			}
			else {
				InetAddress clientIPAddress = inPacket.getAddress();
				System.out.println("client Address: "+clientIPAddress);
				int port = inPacket.getPort();
				custom(parts, serverSocket, clientIPAddress, port);
			}

			splitter = null;
			inPacket = null;
			parts = null;
			inputLine = "";
			negativeAckArr = null;
			successfulySent = 0;
			serverSocket.close();
			System.out.println("Server: Waiting for file request");
		}//while
	}

	private static void custom(byte[][] parts, DatagramSocket serverSocket, InetAddress clientIPAddress, int port) throws IOException {
		DatagramPacket outPacket;
		int offset = 0;
		byte[] header;
		int numParts = parts.length;
		System.out.println("numParts: "+numParts);
	
		//resending if didnt recieve ack
		long time = 0;
		// int resendTimeout = 5;
		do {
			// if(resendTimeout == 0) break;

			if(negativeAckArr != null) {
				for (int j=0; j<negativeAckArr.length; j++) {
					// System.out.println("Server: resending lost packets");
					header = buildHeader((negativeAckArr[j]+1), numParts);
					byte[] outData = new byte[parts[negativeAckArr[j]].length];
					outData = concatBytes(header,parts[negativeAckArr[j]]);
					outPacket = new DatagramPacket(outData, outData.length, clientIPAddress, port);
					serverSocket.send(outPacket);
				}
			}//for j

			System.out.println("Resending from: "+successfulySent);

			for (int i = successfulySent; i < numParts; i++) {
				// System.out.println("Server: sending packets");
				header = buildHeader((i+1), numParts);
				byte[] outData = new byte[parts[i].length];
				outData = concatBytes(header,parts[i]);
				outPacket = new DatagramPacket(outData, outData.length, clientIPAddress, port);
				serverSocket.send(outPacket);
			}//for i

			time = System.currentTimeMillis();
			// resendTimeout--;
		
		} while(!acknowledgment(numParts, serverSocket, time, clientIPAddress) || successfulySent<numParts);
	
	}//custom


	private static boolean acknowledgment(int numParts, DatagramSocket serverSocket, long time, InetAddress currentClientIPAddress) throws IOException {
		byte[] inData = new byte[1400];
		DatagramPacket inPack = new DatagramPacket(inData, inData.length);
		serverSocket.setSoTimeout(timeout);

		while(System.currentTimeMillis()-time < timeout) {
			try {
				serverSocket.receive(inPack);
			} 
			catch(SocketTimeoutException ste) {}

			//check that its not a request from a 3rd node
			// if(!(inPack.getAddress()).equals(currentClientIPAddress)) {
			// 	continue;
			// }

			if(inPack.getData().length > 0) {
				negativeAckArr = new int[2];
				negativeAckArr[0] = readHeader(inPack.getData(), 1);
				negativeAckArr[1] = readHeader(inPack.getData(), 2);
				successfulySent = readHeader(inPack.getData(), 0);
				System.out.println("Server: recieved ack for (" +successfulySent+ "/" +numParts+ ")");
				// if(successfulySent==0)
					// return false;
				return true;
			}
		}//while
	
		return false;
	}

	private static int readHeader(byte[] bytes, int offset) {
		int start = 0, end = 4;
		switch(offset) {
			case 1:
				start = 4;
				end = 8;
				break;
			case 2:
				start = 8;
				end = 12;
			default: break;
		}
		byte[] tmp = Arrays.copyOfRange(bytes, start, end);
		return ByteBuffer.wrap(tmp).getInt();
	}

	private static byte[] buildHeader(int idx, int total) {
		ByteBuffer buff = ByteBuffer.allocate(4);
		byte[] a = buff.putInt(idx).array();
		buff = ByteBuffer.allocate(4);
		byte[] b = buff.putInt(total).array();
		return concatBytes(a,b);
	}

	private static byte[] concatBytes(byte[] a, byte[] b) {
		byte[] tmp = new byte[a.length + b.length];
		for (int i=0; i<tmp.length; i++) {
			tmp[i] = (i < a.length) ? a[i] : b[i-a.length];
		}
		return tmp;
	}
}