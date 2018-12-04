package automail;

import strategies.IMailPool;

/* 
* @author Xu Lin
*/
public class BigRobot extends Robot {

	// Big Robot: Big capacity (6), Strong
	public BigRobot(IMailDelivery delivery, IMailPool mailPool) {
		super(delivery, mailPool, BIG_MAX_CAPACITY, STRONG);
	}
	
}
