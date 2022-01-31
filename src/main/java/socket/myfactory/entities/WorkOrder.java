package socket.myfactory.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@Entity(name = "Workorders")
public class WorkOrder implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(
			name = "workorder_id",
			sequenceName = "workorder_id",
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
	@OneToOne(mappedBy = "activeOrder",targetEntity = WorkCenter.class)
	private WorkCenter workcenter;
	
	public WorkOrder() {
	}

	public WorkOrder(String workType, Integer unitAmount,WorkCenter workcenter) {
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

}
