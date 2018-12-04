package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.CarefulRobot;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;
import exceptions.TubeFullException;
import exceptions.FragileItemBrokenException;

public class MyMailPool implements IMailPool {
	private class Item {
		int priority;
		int destination;
		boolean heavy;
		boolean fragile;
		MailItem mailItem;
		// Use stable sort to keep arrival time relative positions
		
		public Item(MailItem mailItem) {
			priority = (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 1;
			heavy = mailItem.getWeight() >= 2000;
			fragile = mailItem.getFragile();
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.priority < i2.priority) {
				order = 1;
			} else if (i1.priority > i2.priority) {
				order = -1;
			} else if (i1.destination < i2.destination) {
				order = 1;
			} else if (i1.destination > i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	private LinkedList<Item> normalPool;
	private LinkedList<Item> fragilePool;
	private LinkedList<Robot> robots;
	private int lightCount;

	public MyMailPool() {
		// Start empty
		normalPool = new LinkedList<Item>();
		fragilePool = new LinkedList<Item>();	// Fragile items are in a separate pool
		lightCount = 0;							// The number of non-fragile light items
		robots = new LinkedList<Robot>();
	}

	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		if (item.fragile) {
			fragilePool.add(item);
		} else {
			normalPool.add(item);
			if (!item.heavy) lightCount++;
		}
		normalPool.sort(new ItemComparator());
		fragilePool.sort(new ItemComparator());
	}
	
	@Override
	public void step() throws FragileItemBrokenException {
		for (Robot robot : (Iterable<Robot>) robots::iterator) {
			fillStorageTube(robot);
		}
	}
	
	private void fillStorageTube(Robot robot) throws FragileItemBrokenException {
		int capacity = robot.getCapacity();
		boolean strong = robot.isStrong();
		StorageTube tube = robot.getTube();
		StorageTube temp = new StorageTube(capacity);
		
		try {
			// if the robot is careful, take exactly one fragile item if there's any
			// else, get as many items as available or as fit
			if (robot instanceof CarefulRobot && !fragilePool.isEmpty()) {
				Item item = fragilePool.remove();
				temp.addItem(item.mailItem);
			} else if (strong) {
				while (temp.getSize() < capacity && !normalPool.isEmpty()) {
					Item item = normalPool.remove();
					if (!item.heavy) lightCount--;
					temp.addItem(item.mailItem);
				}
			} else {
				ListIterator<Item> i = normalPool.listIterator();
				while (temp.getSize() < capacity && lightCount > 0 && i.hasNext()) {
					Item item = i.next();
					if (!item.heavy) {
						temp.addItem(item.mailItem);
						i.remove();
						lightCount--;
					}
				}
			}
			
			if (temp.getSize() > 0) {
				while (!temp.isEmpty()) tube.addItem(temp.pop());
				robot.dispatch();
			}
		} catch (TubeFullException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerWaiting(Robot robot) { // assumes won't be there
		if (robot.isStrong()) {
			robots.add(robot); 
		} else {
			robots.addLast(robot); // weak robot last as want more efficient delivery with highest priorities
		}
	}

	@Override
	public void deregisterWaiting(Robot robot) {
		robots.remove(robot);
	}

}
