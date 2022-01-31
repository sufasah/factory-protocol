package socket.myfactory.dataaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import socket.myfactory.entities.WorkOrder;

public interface WorkOrderRepo extends JpaRepository<WorkOrder, Long> {
	
	@Query(value = "select w.* from workorders w left outer join workcenters wc on w.id=wc.active_order_id where wc.id is null order by w.work_type", nativeQuery = true)
	public List<WorkOrder> findAllWaitingOrdered();
	
	@Query(value= "select wo.* from workorders wo left outer join workcenters wc on wo.id = wc.active_order_id where wc.active_order_id is null and wo.work_type=?1 limit 1;", nativeQuery = true)
	public Optional<WorkOrder> findOneByWorkTypeWaiting(String workType);
}
