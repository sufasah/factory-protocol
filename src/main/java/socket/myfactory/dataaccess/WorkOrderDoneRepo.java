package socket.myfactory.dataaccess;


import org.springframework.data.jpa.repository.JpaRepository;

import socket.myfactory.entities.WorkOrderDone;

public interface WorkOrderDoneRepo extends JpaRepository<WorkOrderDone, Long> {
	
}
