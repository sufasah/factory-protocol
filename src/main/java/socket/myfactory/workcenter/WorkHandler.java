package socket.myfactory.workcenter;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class WorkHandler extends Thread {
	private Socket socket;
	private PrintStream out;
	private Scanner in;
	private Integer speed;
	
	public Object lock;
	
	public WorkHandler(Socket socket, PrintStream out, Scanner in, Integer speed) {
		this.socket = socket;
		this.out = out;
		this.in = in;
		this.speed = speed; 
	}

	@Override
	public void run(){
		String sline[],command;
		do {
			try {
				sline = in.nextLine().split(" ");
				command = sline[0]; 
				if(command.equals("NEW")) {
						Integer unitAmount = Integer.valueOf(sline[1]);
						System.out.println(String.format("Received work order with amount %d. it will take %d minutes",unitAmount,1L*speed*unitAmount));
						if(unitAmount > 0 && speed > 0) { 
							synchronized(this) {						
								wait(1000L*60*speed*unitAmount);
							}
						}
						System.out.println("work is done.");
						out.println("DONE");
				} else if(command.equals("ERROR")) {					
					System.out.println("Connection closed because of server error...");
					System.exit(0);
				} 
				else if(command.equals("ILLEGAL")) {
					System.out.println("Sending data has not valid protocol standards.");
				}
				else if(command.equals("CLOSE")) {
					System.out.println("Server sent close signal. Connection closing...");
					try {
						socket.close(); 
					} catch (IOException e) {e.printStackTrace();}
					System.exit(0);
				}
			} catch (InterruptedException e) {
				break;
			} catch (NumberFormatException| IndexOutOfBoundsException e) {}
			catch (NoSuchElementException e) {
				System.out.println("Connection lost...");
				System.exit(0);
			}
		}while(true);
		
	}
	
}
