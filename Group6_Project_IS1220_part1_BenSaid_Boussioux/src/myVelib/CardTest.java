package myVelib;

import static org.junit.Assert.*;


import org.junit.Test;

public class CardTest {

	@Test
	public void test() {
		Id id = new Id();
		User user = new User("Leonard");
		System.out.println(user);
		
		Card cardTest = new BlueCard(user);
		user.setCard(cardTest);
		cardTest.creditTime();
		cardTest.creditTime();

		System.out.println(user.getCard().getTimeCredit());
		System.out.println(user);
		
	}

}
