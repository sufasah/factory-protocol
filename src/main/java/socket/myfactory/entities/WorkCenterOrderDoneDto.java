package socket.myfactory.entities;

import socket.myfactory.entities.WorkCenter.WorkCenterStatus;

public interface WorkCenterOrderDoneDto{

	public Long getId();

	public String getName();

	public String getWorkType();

	public Integer getSpeed();

	public WorkCenterStatus getStatus();
	
	public Long getActiveOrderId();
	
	public Long getWorkOrderId();

	public Integer getUnitAmount();
	
}
