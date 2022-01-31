package socket.myfactory.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import socket.myfactory.dataaccess.UserRepo;
import socket.myfactory.dataaccess.WorkCenterRepo;
import socket.myfactory.dataaccess.WorkOrderDoneRepo;
import socket.myfactory.dataaccess.WorkOrderRepo;
import socket.myfactory.entities.WorkCenter;
import socket.myfactory.entities.WorkOrder;
import socket.myfactory.entities.WorkCenter.WorkCenterStatus;

@Service
public class MyFactoryServer {
	
	public static final int PORT = 9999;
	private static final long CHECK_TIME_INTERVAL=1000*60;
	public static final Object SELECTONEORDERLOCK = new Object();
	
	private ServerSocket serverSocket;
	
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private WorkCenterRepo workCenterRepo;
	@Autowired
	private WorkOrderRepo workOrderRepo;
	@Autowired
	private WorkOrderDoneRepo workOrderDoneRepo;
	
	private Map<Long, ClientHandler> workcenterIdToHandler; 
	
	private ArrayList<ClientHandler> allHandlers;
	
	public MyFactoryServer(UserRepo userRepo, WorkCenterRepo workCenterRepo, WorkOrderRepo workOrderRepo) {
		this.userRepo = userRepo;
		this.workCenterRepo = workCenterRepo;
		this.workOrderRepo = workOrderRepo;
		workcenterIdToHandler = Collections.synchronizedMap(new HashMap<Long,ClientHandler>());
		allHandlers = new ArrayList<ClientHandler>();
	}
	
	public void startServer() throws Exception{
		workCenterRepo.deleteAll();
		serverSocket = new ServerSocket(PORT);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				for (ClientHandler clientHandler : allHandlers) {
					try {
						clientHandler.out.println("CLOSE");
						clientHandler.client.close();
					} catch (IOException e) {}
				} 
				try {
					serverSocket.close();
				} catch (IOException e1) {}
			}
		}));
		
		do
		{
			Socket client = serverSocket.accept();

			System.out.println("\nNew client accepted.\n");

			ClientHandler handler = new ClientHandler(
					client,
					userRepo,
					workCenterRepo,
					workOrderRepo,
					workOrderDoneRepo,
					workcenterIdToHandler
					);

			handler.start();
			
			allHandlers.add(handler);
		}while (true);
		
	}
	
	 
	@Async
	public void handleAssignmentWorkcenterOrders() {
		while(true) {			
			try {				
				try {
					Thread.sleep(CHECK_TIME_INTERVAL);
				} catch (InterruptedException e) {}
				
				List<WorkCenter> emptyCenters = workCenterRepo.findAllEmpty();
				for (WorkCenter workCenter : emptyCenters) {
					ClientHandler handler = null;
					Optional<WorkOrder> woo;
					
					synchronized (MyFactoryServer.SELECTONEORDERLOCK) {
						woo= workOrderRepo.findOneByWorkTypeWaiting(workCenter.getWorkType());						
						if(woo.isPresent()) {
							WorkOrder wo = woo.get();
							workCenter.setActiveOrder(wo);
							workCenter.setStatus(WorkCenterStatus.BUSY);
							workCenterRepo.save(workCenter);
							handler = workcenterIdToHandler.get(workCenter.getId());
						}
					}
					
					if(handler!=null)
						handler.out.println(String.format("NEW %d",woo.get().getUnitAmount()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
