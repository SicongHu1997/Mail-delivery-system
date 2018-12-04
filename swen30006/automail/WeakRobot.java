package automail;

import strategies.IMailPool;

/* 
* @author Xu Lin
*/
public class WeakRobot extends Robot {

	// Weak Robot: Standard capacity (4), Weak
	public WeakRobot(IMailDelivery delivery, IMailPool mailPool) {
		super(delivery, mailPool, STANDARD_MAX_CAPACITY, WEAK);
	}

}
