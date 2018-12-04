package automail;

import strategies.IMailPool;

/* 
* @author Xu Lin 
*/
public class StandardRobot extends Robot {

	// Standard Robot: Standard capacity (4), Strong
	public StandardRobot(IMailDelivery delivery, IMailPool mailPool) {
		super(delivery, mailPool, STANDARD_MAX_CAPACITY, STRONG);
	}

}
