package socket.myfactory.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

import javax.transaction.Transactional;

import socket.myfactory.dataaccess.UserRepo;
import socket.myfactory.dataaccess.WorkCenterRepo;
import socket.myfactory.dataaccess.WorkOrderDoneRepo;
import socket.myfactory.dataaccess.WorkOrderRepo;
import socket.myfactory.entities.User;
import socket.myfactory.entities.WorkCenter;
import socket.myfactory.entities.WorkCenterOrderDoneDto;
import socket.myfactory.entities.WorkOrder;
import socket.myfactory.entities.WorkOrderDone;
import socket.myfactory.entities.WorkCenter.WorkCenterStatus;

class ClientHandler extends Thread {
	
	public Socket client;
	public Scanner in;
	public PrintWriter out;
	
	private UserRepo userRepo;
	private WorkCenterRepo workCenterRepo;
	private WorkOrderRepo workOrderRepo;
	@SuppressWarnings("unused")
	private WorkOrderDoneRepo workOrderDoneRepo;
	
	Map<Long,ClientHandler> workCenterIdToHandler;
	
	Boolean isUser;
	Long id;
	
	
	public ClientHandler(Socket socket,UserRepo ur, WorkCenterRepo wcr, WorkOrderRepo wor,WorkOrderDoneRepo wodr, Map<Long,ClientHandler> workCenterIdToHandler)	{
		client = socket;
		userRepo = ur;
		workCenterRepo = wcr;
		workOrderRepo = wor;
		workOrderDoneRepo = wodr;
		isUser = null;
		id = null;

		this.workCenterIdToHandler = workCenterIdToHandler;

		try {
			in = new Scanner(client.getInputStream());
			out = new PrintWriter(client.getOutputStream(),true);
		}
		catch(IOException ioEx) {
			ioEx.printStackTrace();
		}
		
		
	}
	
	public void run() {
		try {
			String[] sline;
			do {
				try {
					String command; 
					sline = in.nextLine().split(" ");
					command = sline[0]; 
					
					if(isUser == null) {				
						if(command.equals("LOGIN")) {
							String username = sline[1];
							String password = sline[2];
								
							User user = userRepo.login(username, password);
							
							if(user != null) {
								out.println("LOG");
	
								isUser=true;
								id = user.getId();
							}
							else
								out.println("NLOG");
							
						}
						else if(command.equals("CON")) {
							
							String name = sline[1];
							String workType = sline[2];
							int speed = Integer.valueOf(sline[3]);
							
							WorkCenter e = new WorkCenter(name,workType,speed,WorkCenterStatus.EMPTY,null,null);
							e = workCenterRepo.save(e);
							
							synchronized(workCenterIdToHandler) {							
								workCenterIdToHandler.put(e.getId(),this);
							}
							
							out.println(e.getId());
							
							isUser=false;
							id = e.getId();
						} 
						else if(command.equals("CLOSE")) {			
							closeConnection();		
							return;
						}
						else {
							out.println("ILLEGAL");
						}
					}else {					
						if(isUser && command.equals("OP")) {
							int opcode = Integer.valueOf(sline[1]);
							String line = null;
							if(opcode != 3) {
								line = in.nextLine();
								if(line.equals("CLOSE")) {
									closeConnection();
									return;
								}								
							}
							switch(opcode) {
							case 1:
								op1(line);
								break;
							
							case 2: 
								op2(Long.valueOf(line)); 
								break;
							
							case 3: 
								op3();  
								break; 
							
							case 4:
								sline = line.split(" ");
								op4(sline[0],Integer.valueOf(sline[1]));
								break;
							}
							
						}
						else if(!isUser && command.equals("DONE")) {						
							Integer amount = doneTransaction();
							if(amount!=null)
								out.println(String.format("NEW %d",amount));;
						}
						else if(command.equals("CLOSE")) {			
							closeConnection();		
							return;
						}
						else {
							out.println("ILLEGAL");
						}
					}
				
				}
				catch (NumberFormatException | IndexOutOfBoundsException e) {
					out.println("ILLEGAL");
				} catch (NoSuchElementException e) {
					closeConnection();
					return;
				}
			}while (true);
			
		}
		catch(Exception e) {
			out.println("ERROR");
			e.printStackTrace();
		}
		
		closeConnection();
	}
	
	public void op1(String workType) {
		
		List<WorkCenter> res = workCenterRepo.findAllByWorkType(workType);
		
		out.println(res.size());
		
		for (WorkCenter workCenter : res) {
			out.println(String.format("%d %s %s %d %s %d", 
					workCenter.getId(),
					workCenter.getName(),
					workCenter.getWorkType(),
					workCenter.getSpeed(),
					workCenter.getStatus().toString(),
					workCenter.getActiveOrder()==null ? -1 : workCenter.getActiveOrder().getId()
					));
		}
	}
	
	public void op2(Long workCenterId) {
		
		List<WorkCenterOrderDoneDto> res = workCenterRepo.findByIdWithWorkOrdersDone(workCenterId);
		
		if(res.size()==0) {
			Optional<WorkCenter> oe = workCenterRepo.findById(workCenterId);
			if(oe.isPresent()) {
				WorkCenter e = oe.get();
				out.println(String.format("%d %s %s 0", 
						e.getId(),
						e.getName(),
						e.getStatus().toString()
						));
			}else {
				out.println("-1");
			}
			return;
		}

		out.println(String.format("%d %s %s %d", 
				res.get(0).getId(),
				res.get(0).getName(),
				res.get(0).getStatus().toString(),
				res.size()
				));	
		
		for (WorkCenterOrderDoneDto workCenterOrderDone : res) {
			out.println(String.format("%d %s %d",
					workCenterOrderDone.getWorkOrderId(),
					workCenterOrderDone.getWorkType(),
					workCenterOrderDone.getUnitAmount()
					));
		}
	}
	
	private void op3() {
		
		List<WorkOrder> res = workOrderRepo.findAllWaitingOrdered();
		
		out.println(res.size());
		
		for (WorkOrder workOrder: res) {
			out.println(String.format("%d %s %d",
					workOrder.getId(),
					workOrder.getWorkType(),
					workOrder.getUnitAmount()
					));
		}
	}
	
	private void op4(String workType,int unitAmount){
		
		WorkOrder e = workOrderRepo.save(new WorkOrder(workType,unitAmount,null));
		
		out.println(String.format("%d",
				e.getId()
				));
	}
	
	@Transactional(rollbackOn = Exception.class)
	private Integer doneTransaction() {
		Integer retRes=null;
		WorkCenter e = workCenterRepo.findById(id).get();
		WorkOrder a = e.getActiveOrder();
		a.setWorkcenter(null);
		
		WorkOrderDone wod = new WorkOrderDone(a.getWorkType(),a.getUnitAmount(),e);		
		e.setWorkcenterOrdersDone(Arrays.asList(wod));
		
		synchronized (MyFactoryServer.SELECTONEORDERLOCK) {			
			Optional<WorkOrder> woo = workOrderRepo.findOneByWorkTypeWaiting(e.getWorkType());
			if(woo.isPresent()) {
				WorkOrder wo = woo.get();
				e.setActiveOrder(wo);
				wo.setWorkcenter(e);
				e.setStatus(WorkCenterStatus.BUSY);
				retRes=wo.getUnitAmount();
			}
			else {
				e.setActiveOrder(null);			
				e.setStatus(WorkCenterStatus.EMPTY);
			}
			
			workCenterRepo.save(e);
			workOrderRepo.delete(a);
			return retRes;
		}
	}
	
	public void closeConnection() {
		try {
			if (client!=null) 
				client.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Connection could not closed...");
		}
		
		try {
			if(isUser != null && !isUser) {
				synchronized(workCenterIdToHandler) {					
					workCenterIdToHandler.remove(id);
				}
				workCenterRepo.deleteById(id);
				isUser=null;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}