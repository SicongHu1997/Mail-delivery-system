package strategies;

import java.util.List;

import automail.BigRobot;
import automail.CarefulRobot;
import automail.IMailDelivery;
import automail.Robot;
import automail.RobotType;
import automail.StandardRobot;
import automail.WeakRobot;

public class Automail {
	      
    public Robot[] robots;
    public IMailPool mailPool;
    private int numRobots;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, List<RobotType> robotTypes) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	numRobots = robotTypes.size();
    	robots = new Robot[numRobots];
    	for (int i = 0; i < numRobots; i++) {
    		switch (robotTypes.get(i)) {
    		case Big:
    			robots[i] = new BigRobot(delivery, mailPool);
    			break;
    		case Careful:
    			robots[i] = new CarefulRobot(delivery, mailPool);
    			break;
    		case Standard:
    			robots[i] = new StandardRobot(delivery, mailPool);
    			break;
    		case Weak:
    			robots[i] = new WeakRobot(delivery, mailPool);
    			break;
    		}
    	}
    }
    
    public int getNumRobots() {
    	return numRobots;
    }
}
