package socket.myfactory.workcenter;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class WorkCenterClient {
	public static final int PORT = 9999;
	public static final String HOST = "localhost";

	public static void skipLine(Scanner sysin) {
		sysin.skip(Pattern.compile(".*\\r?\\n"));		
	}
	
	public static void main(String[] args) {
		Socket socket;
		try {
			
			
			Scanner sysin = new Scanner(System.in);
			Long id;
			String name,workType,speed;

			System.out.print("Name: ");
			name = sysin.next();
			skipLine(sysin);
			System.out.print("WorkType: ");
			workType = sysin.next();
			skipLine(sysin); 
			System.out.print("Speed: ");
			speed = sysin.next();
			skipLine(sysin);

			socket = new Socket(HOST,PORT);
			Scanner in = new Scanner(socket.getInputStream());
			PrintStream out = new PrintStream(socket.getOutputStream());
			
			out.println(String.format("CON %s %s %s",
					name,
					workType,
					speed
					));
			
			String line = in.nextLine();
			
			if(line.equals("ERROR")) {
				System.out.println("Connection failed because of server error...");
				System.exit(0);
			} 
			else if(line.equals("ILLEGAL")) {
				System.out.println("Sending data has not valid protocol standards.");
				System.exit(1);
			}
			else if(line.equals("CLOSE")) {
				System.out.println("Server sent close signal. Connection closing...");
				try {
					socket.close();
				} catch (IOException e) {e.printStackTrace();}
				System.exit(0);
			}
			
			id = Long.valueOf(line);
			
			System.out.println("Id received "+id);
			
			
			
			System.out.println("Info: To close connection type 'close' and press enter.");
			WorkHandler handler = new WorkHandler(socket,out,in,Integer.valueOf(speed));
			handler.start();
			
			
			String read;
			do {
				read = sysin.nextLine();
			}while(!read.equals("close"));
			
			try {
				out.println("CLOSE");
				socket.close();
				System.out.println("Connection closed...");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
			sysin.close();
		} catch (UnknownHostException e) {
			System.out.println("Host could not resolved...");
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch (NoSuchElementException e) {
			System.out.println("Connection lost...");
			System.exit(0);
		}
		System.exit(1);		
	}
}
