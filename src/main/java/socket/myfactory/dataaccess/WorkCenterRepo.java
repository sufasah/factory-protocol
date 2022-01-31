package socket.myfactory.dataaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import socket.myfactory.entities.WorkCenter;
import socket.myfactory.entities.WorkCenterOrderDoneDto;

public interface WorkCenterRepo extends JpaRepository<WorkCenter, Long>{
	
	@Query("select w from Workcenters w where w.workType=?1")
	public List<WorkCenter> findAllByWorkType(String workType);
	
	@Query("select w from Workcenters w where w.status='EMPTY'")
	public List<WorkCenter> findAllEmpty();
	
	@Query(value = "select w.id, w.name, w.work_type as workType, w.speed, w.status, w.active_order_id as activeOrderId, wd.id as workOrderId, wd.unit_amount as unitAmount from workcenters w, workordersdone wd where w.id=?1 and w.id = wd.workcenter_id",nativeQuery = true)
	public List<WorkCenterOrderDoneDto> findByIdWithWorkOrdersDone(Long id); 
	
	@Query(value = "select * from workcenters w where w.work_type=?1 and w.status = 'EMPTY' limit 1", nativeQuery = true)
	public Optional<WorkCenter> findEmptyOneWithType(String workType);
	
}
