package example2;

import java.io.Serializable;
import java.util.UUID;

public class Purchase implements Serializable {
	public UUID id;
	public String buyerId;
	public Status purchaseStatus;
	private static final long serialversionUID = 129348938L; 
	
	public Purchase(String buyerId, Status purchaseStatus) {
		this.id = UUID.randomUUID();
		this.buyerId = buyerId;
		this.purchaseStatus = purchaseStatus;
	}
	
	public String toString() {
		return "id: " + this.id + ", buyerId: " + this.buyerId + ", purchaseStatus: " + this.purchaseStatus;
	}
}
