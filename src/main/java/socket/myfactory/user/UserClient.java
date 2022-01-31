package socket.myfactory.user;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import socket.myfactory.entities.WorkCenter.WorkCenterStatus;

public class UserClient {
	public static final int PORT = 9999;
	public static final String HOST = "localhost";
	
	public static Socket socket;
	public static Scanner in;
	public static Scanner sysin;
	public static PrintStream out;
	public static String username;
	public static String password; 
	public static LinkedBlockingQueue<Thread> opQueue;
	public static ThreadManager manager;
	
	public static void skipLine() {
		sysin.skip(Pattern.compile(".*\\r?\\n"));		
	}
	
	public static void serverError() {
		System.out.println("Connection failed because of server error...");
		System.exit(0);
	}
	public static void closeSignal() {
		System.out.println("Server sent close signal. Connection closing...");
		try {
			socket.close();
		} catch (IOException e) {e.printStackTrace();}
		System.exit(0);
	}
	public static void connectionLost() {
		System.out.println("Connection lost...");
		try {
			socket.close();
		} catch (IOException e) {e.printStackTrace();}
		System.exit(0);
	}
	
	public static void main(String[] args) {
		try {
			opQueue = new LinkedBlockingQueue<Thread>();
			sysin = new Scanner(System.in);
			
			
			
			socket = new Socket(HOST,PORT);
			in = new Scanner(socket.getInputStream());
			out = new PrintStream(socket.getOutputStream());

			while(true) {				
				System.out.print("username: ");
				username = sysin.next();
				skipLine();
				System.out.print("password: ");
				password= sysin.next(); 
				skipLine();

				out.println(String.format("LOGIN %s %s",
						username,
						password
						));
				
				String loggedS = in.nextLine();
				
				if(loggedS.equals("LOG")) {
					System.out.println("Successfully logged.");
					break;
				}
				else if(loggedS.equals("NLOG")) {
					System.out.println("Wrong username or password.");
				}
				else if(loggedS.equals("ERROR")) {
					serverError();
				}
				else if(loggedS.equals("ILLEGAL")) {
					System.out.println("Sending data has not valid protocol standards.");
				}
				else if(loggedS.equals("CLOSE")) {
					System.out.println("Server sent close signal. Connection closing...");
					socket.close();
					System.exit(0);
				}
			}

			manager = new ThreadManager(opQueue);
			manager.start();
			
			do {
				System.out.print(
						"1. Get list of workcenters by type\n"
						+ "2. Get a workcenter with work orders done by id\n"
						+ "3. Get list of work orders waiting to be assigned one of workcenters grouped by work types\n"
						+ "4. Add new work order to the server\n"
						+ "5. Exit\n"
						+ "Operation : ");
				String op = sysin.next();
				skipLine();
				
				if(op.equals("1")) {
					System.out.print("WorkType: ");
					String workType = sysin.next();
					skipLine();
					opQueue.add(new Thread(new Runnable() {public void run() {	
						try {
							op1(workType);
						} catch (NoSuchElementException e) {
							connectionLost();
						}
					}}));
					synchronized (manager.newOpLock) {						
						manager.newOpLock.notify();
					}
				}
				else if(op.equals("2")){
					System.out.print("Id: ");
					String id = sysin.next();
					skipLine();
					
					opQueue.add(new Thread(new Runnable() {public void run() {	
						try {
							op2(id);	
						} catch (NoSuchElementException e) {
							connectionLost();
						}
					}}));
					synchronized (manager.newOpLock) {						
						manager.newOpLock.notify();
					}				}
				else if(op.equals("3")){
					opQueue.add(new Thread(new Runnable() {public void run() {		
						try {
							op3();		
						} catch (NoSuchElementException e) {
							connectionLost();
						}
					}}));
					synchronized (manager.newOpLock) {						
						manager.newOpLock.notify();
					}				}
				else if(op.equals("4")){
					System.out.print("WorkType: ");
					String workType = sysin.next();
					skipLine();
					System.out.print("UnitAmount: ");
					String unitAmount = sysin.next();
					skipLine();
					
					opQueue.add(new Thread(new Runnable() {public void run() {		
						try {
							op4(workType,unitAmount);
						} catch (NoSuchElementException e) {
							connectionLost();
						}
					}}));
					synchronized (manager.newOpLock) {						
						manager.newOpLock.notify();
					}				}
				else if(op.equals("5")){
					break;
				}
				else {
					System.out.println("Invalid input!");
					continue;
				}
			}while(true);
			
			try {
				out.println("CLOSE");
				socket.close();
				System.out.println("Connection closed...");
			} catch (IOException e) {
				e.printStackTrace();
			}		
			
			System.exit(0);
		} catch (UnknownHostException e) {
			System.out.println("Host could not resolved.");
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(NoSuchElementException e) {
			connectionLost();
		}
		System.exit(1);
	}

	private static void op4(String workType, String unitAmount) {
		synchronized (manager.lock) {			
			out.println("OP 4");
			out.println(String.format("%s %s", workType,unitAmount));
			
			String line = in.nextLine();
			if(line.equals("ERROR")) {
				serverError();
			}
			else if(line.equals("ILLEGAL")) {
				System.out.println("Sending data has not valid protocol standards.");
				return;
			}
			else if(line.equals("CLOSE")) {
				closeSignal();
			}
			
			System.out.println("[ OPERATION 4 ]");
			System.out.println("RECEIVED ID FOR THE ORDER: "+ Long.valueOf(line));
		}
	}

	private static void op3() {
		synchronized (manager.lock) {
			out.println("OP 3");
			
			String line = in.nextLine();
			
			if(line.equals("ERROR")) {
				serverError();
			}
			else if(line.equals("ILLEGAL")) {
				System.out.println("Sending data has not valid protocol standards.");
				return;
			}
			else if(line.equals("CLOSE")) {
				closeSignal();
			}
			
			int count = Integer.valueOf(line);
			
			if(count>0) {
				Long id;
				String workType;
				Integer unitAmount;
				
				String[] sline = in.nextLine().split(" ");
				if(sline[0].equals("CLOSE")) {
					closeSignal();
				}
				
				id = Long.valueOf(sline[0]);
				workType = sline[1];
				unitAmount = Integer.valueOf(sline[2]);
				
				System.out.println("[ OPERATION 3 ]");
				System.out.println("--------------"+workType.toUpperCase()+"--------------");
				System.out.println(String.format("%d %s %d",id,workType,unitAmount));
				
				for(int i=1; i<count; i++) {
					String nworkType;
					
					sline = in.nextLine().split(" ");
					if(sline[0].equals("CLOSE")) {
						closeSignal();
					}
					
					id = Long.valueOf(sline[0]);
					nworkType = sline[1];
					unitAmount = Integer.valueOf(sline[2]);
					
					if(!workType.equals(nworkType))
						System.out.println("--------------"+nworkType.toUpperCase()+"--------------");
					
					workType=nworkType;
					System.out.println(String.format("%d %s %d",id,workType,unitAmount));
				}
			}
		}
	}

	private static void op2(String sid) {
		synchronized (manager.lock) {
			out.println("OP 2");
			out.println(sid);
			
			String line = in.nextLine();
			
			if(line.equals("ERROR")) {
				serverError();
			}
			else if(line.equals("ILLEGAL")) {
				System.out.println("Sending data has not valid protocol standards.");
				return;
			}
			else if(line.equals("CLOSE")) {
				closeSignal();
			}
			
			String[] sline = line.split(" ");
			
			Long id;
			String name;
			WorkCenterStatus status;
			
			id = Long.valueOf(sline[0]);
			System.out.println("[ OPERATION 2 ]");
			if(id==-1){
				System.out.println("Workcenter not found...");
				return;
			}
			name = sline[1];
			status = WorkCenterStatus.valueOf(sline[2]);
			
			int count = Integer.valueOf(sline[3]);
			
			System.out.println(String.format("WORK CENTER INFOS -> ID=%d NAME=%s STATUS=%s",id,name,status.toString()));
			System.out.println("--------------WORK ORDERS DONE--------------");
			
			for(int i=0;i<count;i++) {
				Long id2;
				String workType;
				Integer unitAmount;
				
				sline = in.nextLine().split(" ");
				if(sline[0].equals("CLOSE")) {
					closeSignal();
				}
				
				id2 = Long.valueOf(sline[0]);
				workType = sline[1];
				unitAmount = Integer.valueOf(sline[2]);
				
				System.out.println(String.format("%d %s %d", id2,workType,unitAmount));
			}	
		}
	}

	private static void op1(String pworkType) {
		synchronized (manager.lock) {
			out.println("OP 1");
			out.println(pworkType);
			
			String line = in.nextLine();
			
			if(line.equals("ERROR")) {
				serverError();
			}
			else if(line.equals("ILLEGAL")) {
				System.out.println("Sending data has not valid protocol standards.");
				return;
			}
			else if(line.equals("CLOSE")) {
				closeSignal();
			}
			
			int count = Integer.valueOf(line);
			
			Long id,activeOrderId;				
			String name,workType;
			Integer speed;
			WorkCenterStatus status;
			String sline[];
			
			System.out.println("[ OPERATION 1 ]");
			for(int i=0; i<count; i++) {
				
				sline = in.nextLine().split(" ");
				if(sline[0].equals("CLOSE")) {
					closeSignal();
				}
				
				id = Long.valueOf(sline[0]);
				name = sline[1];
				workType = sline[2];
				speed = Integer.valueOf(sline[3]);
				status = WorkCenterStatus.valueOf(sline[4]);
				activeOrderId = Long.valueOf(sline[5]);
				
				System.out.println(String.format("%d %s %s %d %s %d",id,name,workType,speed,status.toString(),activeOrderId));
			}
		}
	}
}

class ThreadManager extends Thread{
	LinkedBlockingQueue<Thread> opQueue;
	public Object lock;
	public Object newOpLock;
	
	public ThreadManager(LinkedBlockingQueue<Thread> opq) {
		opQueue=opq;
		
		lock = new Object();
		newOpLock = new Object();
	} 
	
	public void run() {
		try {
			while(true) {
				if(opQueue.size()>0) {
					Thread t = opQueue.take();
					synchronized (lock) {							
						t.start();
						
					}
				}else {					
					synchronized (newOpLock) {						
						newOpLock.wait();
					}
				}
			}
		} catch (InterruptedException e) {}
	}
}
