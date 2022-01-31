package socket.myfactory.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity(name = "Workordersdone")
public class WorkOrderDone implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(
			name = "workorder_done_id",
			sequenceName = "workorder_done_id",
			allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(
			name="id",
			updatable = false)
	private Long id;
	@Column(name = "work_type",nullable = false)
	private String workType;
	@Column(name = "unit_amount",nullable = false)
	private Integer unitAmount;
	@ManyToOne(targetEntity = WorkCenter.class, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "workcenter_id")
	private WorkCenter workcenter;
	
	public WorkOrderDone() {
	}

	public WorkOrderDone(String workType, Integer unitAmount, WorkCenter workcenter) {
		this.workType = workType;
		this.unitAmount = unitAmount;
		this.workcenter = workcenter;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public Integer getUnitAmount() {
		return unitAmount;
	}

	public void setUnitAmount(Integer unitAmount) {
		this.unitAmount = unitAmount;
	}

	public WorkCenter getWorkcenter() {
		return workcenter;
	}

	public void setWorkcenter(WorkCenter workcenter) {
		this.workcenter = workcenter;
	}

	public WorkOrderDone(String workType, Integer unitAmount) {
		this.workType = workType;
		this.unitAmount = unitAmount;
	}

}
