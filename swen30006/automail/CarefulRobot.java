package automail;

import strategies.IMailPool;

/* 
* @author Xu Lin
*/
public class CarefulRobot extends Robot {
	
	private int stepCount = 0;

	// Careful Robot: Small capacity (3), Strong
	public CarefulRobot(IMailDelivery delivery, IMailPool mailPool) {
		super(delivery, mailPool, SMALL_MAX_CAPACITY, STRONG);
		stepCount = 0;
	}
	
	/**
     * Generic function that moves the careful robot towards the destination
     * Doesn't break fragile items, but moves slower (one floor per two steps)
     * @param destination the floor towards which the robot is moving
     */
	@Override
	public void moveTowards(int destination) {
    	if (stepCount % 2 == 0) {
        	if (getFloor() < destination) {
        		moveUpstairs();
        	} else {
        		moveDownstairs();
        	}
    	}
    }

}
