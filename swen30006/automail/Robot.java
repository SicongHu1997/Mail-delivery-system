package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.FragileItemBrokenException;
import strategies.IMailPool;
import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public class Robot {
	
	public static final int BIG_MAX_CAPACITY = 6;
	public static final int STANDARD_MAX_CAPACITY = 4;
	public static final int SMALL_MAX_CAPACITY = 3;
	public static final boolean STRONG = true;
	public static final boolean WEAK = false;

	StorageTube tube;
    IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public RobotState current_state;
    private int current_floor;
    private int destination_floor;
    private IMailPool mailPool;
    private boolean receivedDispatch;
    private int capacity;
    private boolean strong;
    
    private MailItem deliveryItem;
    
    private int deliveryCounter;
    

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     * @param strong is whether the robot can carry heavy items
     * @param capacity is the tube capacity of robot
     */
    public Robot(IMailDelivery delivery, IMailPool mailPool, int capacity, boolean strong) {    	
    	id = "R" + hashCode();
    	current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        tube = new StorageTube(capacity);
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
        this.capacity = capacity;
        this.strong = strong;
    }
    
    public void dispatch() {
    	receivedDispatch = true;
    }
    
    public int getCapacity() {
    	return capacity;
    }
    
    public boolean isStrong() {
    	return strong;
    }
    
    public int getFloor() {
    	return current_floor;
    }
    
    public void moveUpstairs() {
    	current_floor++;
    }
    
    public void moveDownstairs() {
    	current_floor--;
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void step() throws ExcessiveDeliveryException, ItemTooHeavyException, FragileItemBrokenException {    	
    	switch(current_state) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == Building.MAILROOM_LOCATION){
                	while(!tube.isEmpty()) {
                		MailItem mailItem = tube.pop();
                		mailPool.addToPool(mailItem);
                        System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), mailItem.toString());
                	}
        			/** Tell the sorter the robot is ready */
        			mailPool.registerWaiting(this);
                	changeState(RobotState.WAITING);
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);
                	break;
                }
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!tube.isEmpty() && receivedDispatch) {
                	receivedDispatch = false;
                	deliveryCounter = 0; // reset delivery counter
        			setRoute();
        			mailPool.deregisterWaiting(this);
                	changeState(RobotState.DELIVERING);
                }
                break;
    		case DELIVERING:
    			if (current_floor == destination_floor) { // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
                    delivery.deliver(deliveryItem);
                    deliveryCounter++;
                    if (deliveryCounter > capacity) {  // Implies a simulation bug
                    	throw new ExcessiveDeliveryException();
                    }
                    /** Check if want to return, i.e. if there are no more items in the tube*/
                    if (tube.isEmpty()) {
                    	changeState(RobotState.RETURNING);
                    }
                    else {
                        /** If there are more items, set the robot's route to the location to deliver the item */
                        setRoute();
                        changeState(RobotState.DELIVERING);
                    }
    			} else {
	        		/** The robot is not at the destination yet, move towards it! */
	                moveTowards(destination_floor);
    			}
                break;
    	}
    }

    /**
     * Sets the route for the robot
     * @throws ItemTooHeavyException if the robot is carrying items heavier than it can take
     */
    private void setRoute() throws ItemTooHeavyException{
        /** Pop the item from the StorageUnit */
        deliveryItem = tube.pop();
        if (!strong && deliveryItem.weight > 2000) throw new ItemTooHeavyException(); 
        /** Set the destination floor */
        destination_floor = deliveryItem.getDestFloor();
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     * @throws FragileItemBrokenException if the robot (not careful) is carrying fragile items
     */
    public void moveTowards(int destination) throws FragileItemBrokenException {
        if (deliveryItem != null && deliveryItem.getFragile() || !tube.isEmpty() && tube.peek().getFragile()) {
        	throw new FragileItemBrokenException();
        }
        if (current_floor < destination) {
        	moveUpstairs();
        } else {
        	moveDownstairs();
        }
    }
    
    private String getIdTube() {
    	return String.format("%s(%1d/%1d)", id, tube.getSize(), tube.getCapacity());
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState){
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    }

	public StorageTube getTube() {
		return tube;
	}
    
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}
}
