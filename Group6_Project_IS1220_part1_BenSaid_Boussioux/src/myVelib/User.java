package myVelib;

import java.sql.Timestamp;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentSkipListMap;

import Tests.Test;
import myVelib.Bicycle.BicycleType;
import myVelib.ParkingSlot.UnavailableSlotException;
import myVelib.Station.NoMoreAvailableSlotsException;
import myVelib.Station.NoMoreBikeException;



public class User implements Observer {
	
//*****************************************************************//
//							Attributes 							   //
//*****************************************************************//
		
	static int IDuserCounter=0;
	final static double walkingSpeed = 4;
	
	private Network network;
	protected int id;
	private String name;
	private CreditCard creditCard;
	private Card card;
	private GPS position;
	private Ride ride;
	private UserBalance userBalance;
	private Bicycle bicycle; //Pas s�r de l'utilit� de Bicycle
	private ArrayList<Message> messageBox;
	private ArrayList<Observable> observedStations = new ArrayList<Observable>();
	
	/**A map representing a user's history: with Timestamps as keys and UserStates as values.
	 * This type of map stores the key-value pairs in a specific order. This way it is easy to get the last user's state.
	 * 
	 */
	private ConcurrentSkipListMap <Timestamp, Ride> userHistory = new ConcurrentSkipListMap<Timestamp, Ride>();

	
	
	
//*****************************************************************//
//						Constructor 							   //
//*****************************************************************//
	
	/**
	 * Constructor 
	 * @param id
	 * @param card
	 * @param name
	 * @param messageBox
	 */
	public User(String name) {
		super();
		IDuserCounter++;
		this.id=IDuserCounter;
		this.creditCard = new CreditCard(this, 500);
		this.card = this.creditCard;
		this.name = name;
		this.messageBox = new ArrayList <Message>();
		this.position = new GPS(0,0);
		this.userBalance = new UserBalance();
	}
	
	public User() {
		super();
	}
	
	
//*****************************************************************//
//						Getters and Setters 					   //
//*****************************************************************//
	
	
	public int getId() {
		return id;
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public CreditCard getCreditCard() {
		return creditCard;
	}

	public void setCreditCard(CreditCard creditCard) {
		this.creditCard = creditCard;
	}
	

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public GPS getPosition() {
		return position;
	}

	public void setPosition(GPS position) {
		this.position = position;
	}
//Pas s�r de l'utilit� de Bicycle
	public Bicycle getBicycle() {
		return bicycle;
	}

	public void setBicycle(Bicycle bicycle) {
		this.bicycle = bicycle;
	}

	public ArrayList<Message> getMessageBox() {
		return messageBox;
	}

	public void setMessageBox(ArrayList<Message> messageBox) {
		this.messageBox = messageBox;
	}

	public Ride getRide() {
		return ride;
	}
	
	public void setRide(Ride ride) {
		this.ride = ride;
	}

	public ConcurrentSkipListMap<Timestamp, Ride> getUserHistory() {
		return userHistory;
	}

	public void setUserHistory(ConcurrentSkipListMap<Timestamp, Ride> userHistory) {
		this.userHistory = userHistory;
	}

	public static int getIDuserCounter() {
		return IDuserCounter;
	}

	public static void setIDuserCounter(int iDuserCounter) {
		IDuserCounter = iDuserCounter;
	}

	public static double getWalkingspeed() {
		return walkingSpeed;
	}
	
	


//*****************************************************************//
//							Methods 							   //
//*****************************************************************//	

	
// OBSERVER PATTERN
	/** Observers functions
	 * 
	 * @param s
	 */
	
	public void subscribeStation(Observable s){
		s.addObserver(this);
		this.observedStations.add(s);
	}
	
	public void unsuscribe(Observable s){
		s.deleteObserver(this);
		observedStations.remove(s);
	}
	

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Station) {
			Station station = (Station) o;
			this.recieveMessage(new Message("The Station "+station.getName()+" is now "+station.getStatus()));
			System.out.println("The Station "+station.getName()+" is now "+station.getStatus());
		}

	}


	public void recieveMessage(Message m){
		messageBox.add(m);
	}
	
	public void displayMessage(){
		for (Message m : this.messageBox) {
			m.setRead(true);
			System.out.println(m.getText());}
	}
	
	public void removeMessage(Message m){
		messageBox.remove(m);
	}
	

//-----------------------------------------------------------------//

// VISITOR PATTERN	
		
	/**
	 * adds an event to the user's history. Also checks that the events are entered in chronological order.
	 * @param t
	 * @param ps
	 */
	public void updateUserHistory(Timestamp t, Ride ride){
		if(!userHistory.isEmpty()&& userHistory.lastKey().compareTo(t)>0){
			System.out.println("Error, do not enter a time in the past.");
		}
		else{
			userHistory.put(t,ride);
			//System.out.println("The user's history is updated: the user "+ride.toString()+" the bicycle "+ bicycle.toString() + "at time "+t.toString());
		}
	}
	

	

//-----------------------------------------------------------------//

	/**
	 * This function allows the User to drop Off his bicycle.
	 * @param u
	 * @param s
	 */
	
	public void returnBike(Station s, Timestamp t) {
		
		// return the bike to an available ParkingSlot
		try {
			s.addBicycle(this.bicycle, t);
		} catch (myVelib.Station.UnavailableStationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoMoreAvailableSlotsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		this.setBicycle(null);
		
		//We compute the duration of the trip in ms.
		Duration tripDuration = Duration.ZERO;
		tripDuration = tripDuration.plusMillis(t.getTime()-this.ride.getDepartureTime().getTime());
			
		//If the user has a VelibCard, if the Station is Plus we add timeCredit to the card, 
		//then we reduce the number of hours to pay using that timeCredit.
		if (this.card instanceof VelibCard) {
			VelibCard vCard = (VelibCard) this.card;
			if (s.getStationType()==Station.StationType.Plus) {
				vCard.creditTime();
				this.ride.getTimeCredit().plus(Station.plusTimeCredit);
				this.userBalance.getTotalTimeCredit().plus(Station.plusTimeCredit);
			}
			ConcreteCardVisitor.applyVelibBonus(vCard.getTimeCredit(), tripDuration);
		}
				 		
		
		CardVisitor visitor = new ConcreteCardVisitor();
			
		Double cost = (double) 0;
		try {
			cost = this.card.accept(visitor, tripDuration, this.ride.getBicycle().getType());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Please pay " + cost +".");
		
		this.userBalance.setTotalCharges(this.userBalance.getTotalCharges() + cost);
		this.userBalance.getTotalTime().plus(tripDuration);
		
		this.ride.setArrivalTime(t);
		this.ride.setArrivalStation(s);
		this.ride.setDuration(tripDuration);
		this.ride.setCost(cost);
		this.updateUserHistory(this.ride.getDepartureTime(), this.ride);
		this.ride = new Ride();
		
		this.unsuscribe(s);
	}
	
	
				
	/**
	 * This function allows the User to drop on an electrical bicycle.
	 * @param t
	 * @param s
	 * @throws NoMoreElectricalException 
	 */
//	public void rentBikeElectrical(Station s, Timestamp t) throws NoMoreElectricalException, AlreadyHasABikeException {
//		if (s.slotsOccupiedByElectrical()==0)
//			throw new NoMoreElectricalException(); 
//		if (this.getBicycle()!=null)
//			throw new AlreadyHasABikeException();
//		try {
//		int i = s.selectBicycleElectrical();
//		//We get the bicycle
//				Bicycle bicycle = s.getParkingSlots().get(i).getBicycle();
//				// We set free the slot
//				s.getParkingSlots().get(i).becomesFree(t);
//				this.setBicycle(bicycle);
//				// start counter for the user
//				this.updateUserHistory(t, UserAction.dropped_on);
//				//We need to begin the riding time and put something in the TimeStamp
//				s.addEntryToStationHistory(t);
//				s.setNumberOfRentals(s.getNumberOfRentals()+1);
//		}
//		catch(Station.NoMoreElectricalException e){System.out.println("no electrical: "  + e.toString());
//		}	
//	}
	
	/**
	 * This function allows the User to drop on a mechanical bicycle.
	 * @param u
	 * @param s
	 * @throws NoMoreMechanicalException 
	 */
	
//	
//	public void rentBikeMechanical(Station s, Timestamp t) throws NoMoreMechanicalException, AlreadyHasABikeException {
//		if (s.slotsOccupiedByMechanical()==0)
//			throw new NoMoreMechanicalException(); 
//		if (this.getBicycle()!=null)
//			throw new AlreadyHasABikeException();
//		try {
//		int i = s.selectBicycleMechanical();
//		//We get the bicycle
//				Bicycle bicycle = s.getParkingSlots().get(i).getBicycle();
//				// We set free the slot
//				s.getParkingSlots().get(i).becomesFree(t);
//				this.setBicycle(bicycle);
//				// start counter for the user
//				this.updateUserHistory(t, UserAction.dropped_on);
//				//We need to begin the riding time and put something in the TimeStamp
//				s.addEntryToStationHistory(t);
//				s.setNumberOfRentals(s.getNumberOfRentals()+1);
//		}
//		catch(Station.NoMoreMechanicalException e){System.out.println("no electrical: "  + e.toString());
//		}	
//	}
	
	public void rentBike(Station s, Bicycle.BicycleType bType, Timestamp t) throws NoMoreBikesException, AlreadyHasABikeException {
		if (this.getBicycle()!=null) {
			throw new AlreadyHasABikeException();
		}
		else if (s.slotsOccupiedByMechanical()==0 & bType==Bicycle.BicycleType.Mechanical || s.slotsOccupiedByElectrical()==0 & bType==Bicycle.BicycleType.Electrical) {
			throw new NoMoreBikesException();
		}
		
		else {
			try {
				int i = s.selectBicycle(bType);
				Bicycle bicycle = s.getParkingSlots().get(i).getBicycle();
				// We set free the slot
				s.getParkingSlots().get(i).becomesFree(t);
				this.setBicycle(bicycle);
				// start counter for the user
				this.updateUserHistory(t, this.ride);
				
				this.userBalance.setNumberOfRides(this.userBalance.getNumberOfRides()+1);
				
				//We need to begin the riding time and put something in the TimeStamp
				s.addEntryToStationHistory(t);
				s.setNumberOfRentals(s.getNumberOfRentals()+1);
				this.ride.setDepartureStation(s);
				this.ride.setDepartureTime(t);
				this.ride.setBicycle(bicycle);
			}
			catch (NoMoreBikeException e) {
				e.toString();
			}
		}
	}
	

	public void userBalance() {
		
	}
	
	public void planRide(GPS destination,  boolean plus, boolean uniformity, boolean fastest) {
		if (this.ride == null) {
			this.ride = new PlannedRide(this.network, this.position, destination, plus, uniformity, fastest, false);
			System.out.println("We are finding the best path");
		}
		else {
			this.ride = new PlannedRide(this.network, this.position, destination, plus, uniformity, fastest, true);
			System.out.println("You haven't reached your destination yet. We are looking for a new path.");
			
		}
	}
	

//-----------------------------------------------------------------//

// toString METHOD
		
		public String toString() {
			return "User [id=" + id + ", name=" + name + ", card="
					+ card + "]";
		}


	
//*****************************************************************//
//							EXCEPTIONS 							   //
//*****************************************************************//	
	
	public class NoMoreElectricalException extends Exception{
		public NoMoreElectricalException(){
		    System.out.println("Sorry, no more electrical bikes available.");
		  }  
	}
	
	public class NoMoreMechanicalException extends Exception{
		public NoMoreMechanicalException(){
		    System.out.println("Sorry, no more electrical bikes available.");
		  }  
	}
	
	public class NoMoreBikesException extends Exception{
		public NoMoreBikesException(){
		    System.out.println("Sorry, no more bikes of the desired type available.");
		  }  
	}
	
	public class AlreadyHasABikeException extends Exception{
		public AlreadyHasABikeException(){
		    System.out.println("Sorry, you already have a bike.");
		  }  
	}
	
	public class UnavailableStationException extends Exception{
		public UnavailableStationException(){
		    System.out.println("Sorry, this station is unavailable to drop off your bicycle.");
		  }  
	}
	

	
	
//*****************************************************************//
//								Main 							   //
//*****************************************************************//	
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws UnavailableSlotException, NoMoreBikesException, AlreadyHasABikeException {
		
		Network myNetwork = Test.CreateTestNetwork();
		User userTest = new User("Anis");
		userTest.setNetwork(myNetwork);
		userTest.setRide( new PlannedRide(myNetwork, userTest.getPosition(), new GPS(5,5), true, true, false, false));
		System.out.println(userTest.ride.getDepartureStation());
		System.out.println(userTest.ride.getArrivalStation());
		System.out.println(new Timestamp(118,2,22,10,30,0,0));
		userTest.rentBike(userTest.ride.getDepartureStation(), BicycleType.Electrical, new Timestamp(118,2,22,10,30,0,0));
		
		
		
	}



	
}