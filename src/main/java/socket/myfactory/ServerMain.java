package socket.myfactory;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import socket.myfactory.server.MyFactoryServer;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class ServerMain implements CommandLineRunner{
	
	@Autowired
	private MyFactoryServer factoryServer;
	
	public static void main(String[] args) {
		SpringApplication.run(ServerMain.class, args);
	}

	@Override
	public void run(String... args) {
		try {			
			factoryServer.handleAssignmentWorkcenterOrders();
			
			new Thread(new Runnable() {
				public void run() {
					Scanner sc = new Scanner(System.in);
					while(true) {
						if("stop".equals(sc.nextLine()))break;
					}
					sc.close();
					System.out.println("STOPPING THE SERVER ...");
					System.exit(0);
				}
			}).start();;
			
			factoryServer.startServer();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
