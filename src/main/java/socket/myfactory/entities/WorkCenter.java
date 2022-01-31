package socket.myfactory.entities;
import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@Entity(name="Workcenters")
public class WorkCenter implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum WorkCenterStatus{EMPTY,BUSY};
	
	@Id
	@SequenceGenerator(
			name = "workcenter_id",
			sequenceName = "workcenter_id",
			allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(
			name="id",
			updatable = false)
	private Long id;
	@Column(name="name")
	private String name;
	@Column(name = "work_type",nullable = false)
	private String workType;
	@Column(name="speed",nullable = false)
	private Integer speed;
	@Column(name="status",nullable = false)
	@Enumerated(EnumType.STRING)
	private WorkCenterStatus status;
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "active_order_id",foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT,foreignKeyDefinition = "fk_active_order_id"))
	private WorkOrder activeOrder;
	@OneToMany(mappedBy = "workcenter",targetEntity = WorkOrderDone.class,cascade = CascadeType.ALL )
	private List<WorkOrderDone> workcenterOrdersDone;
	
	public WorkCenter() {
	}

	public WorkCenter(String name, String workType, Integer speed, WorkCenterStatus status,
			WorkOrder activeOrder, List<WorkOrderDone> workcenterOrderDone) {
		this.name = name;
		this.workType = workType;
		this.speed = speed;
		this.status = status;
		this.activeOrder = activeOrder;
		this.workcenterOrdersDone = workcenterOrderDone;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	public WorkCenterStatus getStatus() {
		return status;
	}

	public void setStatus(WorkCenterStatus status) {
		this.status = status;
	}

	public WorkOrder getActiveOrder() {
		return activeOrder;
	}

	public void setActiveOrder(WorkOrder activeOrder) {
		this.activeOrder = activeOrder;
	}


	public List<WorkOrderDone> getWorkcenterOrdersDone() {
		return workcenterOrdersDone;
	}

	public void setWorkcenterOrdersDone(List<WorkOrderDone> workcenterOrdersDone) {
		this.workcenterOrdersDone = workcenterOrdersDone;
	}
	
}
