package org.fishwife.jrugged.spring;

import org.fishwife.jrugged.Monitorable;
import org.fishwife.jrugged.Status;

public class MonitorableStub implements Monitorable {

	private Status status = Status.UP;
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}

}
