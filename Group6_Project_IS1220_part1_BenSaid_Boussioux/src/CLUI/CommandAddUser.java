package CLUI;

import java.time.Duration;
import java.util.ArrayList;

import myVelib.*;



public class CommandAddUser extends Command {

	public CommandAddUser(MyVelib myvelib, ArrayList<String> args) throws SyntaxErrorException, MisuseException {
		super(myvelib,args);
	}



	@Override
	public void execute() throws SyntaxErrorException {
		Card card;
		User user = new User();
		MyVelib myVelib = this.getMyVelib();
		String userName = getArgs().get(0);
		String cardType = getArgs().get(1);
		String velibnetworkName = getArgs().get(2);
		Network network = myVelib.getNetwork(velibnetworkName);
		if (network==null) {
			throw new SyntaxErrorException("Please check the network name.");
		}
		if (cardType.equalsIgnoreCase("Vlibre")){
			 user = new User(userName, new VlibreCard(user, Duration.ZERO), network);
		 }
		 else if (cardType.equalsIgnoreCase("VMax")) {
			 user = new User(userName, new VmaxCard(user, Duration.ZERO), network);
		 }
		 else if (cardType.equalsIgnoreCase("CreditCard")){
			 user = new User(userName, new CreditCard(user), network);
		 }
		 else {
			 throw new SyntaxErrorException("Please check the card type.");
			 }
		 network.addUser(user); ;
		 System.out.println("The user "+userName+" has been added to "+velibnetworkName+".");
	}

	@Override
	public void check() throws SyntaxErrorException {
		checkNumOfArgs(3);
	}
}