import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TransactionManager implements ResetSelf {
	
	private Transaction transaction = null;
	private String bookerName;
	private String bookerMobileNo;
	private String bookerEmail;
	private String bookerCreditCard;
	private Scanner sc = new Scanner(System.in);
	public Boolean exit = false;
	
	// Singleton & Constructor
 	private static TransactionManager single_instance = null;
 	
 	private TransactionManager() {}
	
	public static TransactionManager getInstance()
	{
	    if (single_instance == null) {
	    	 single_instance = new TransactionManager();
	    }	        
	    return single_instance;
	}

	
	// Methods
	
	// Start a new transaction with selected seats and selected tickets
	public void startTransaction(List<Ticket> ticketList, Map<TicketType, Double> ticketPrices, Map<TicketType, Integer> ticketCount) {
		
		exit = false;
		int choice;
		String input;
		
		// Create new transaction & update its total price and tickets
		setTransaction(new Transaction());
		updateTotalPrice(ticketPrices, ticketCount);
		getTransaction().setTicketList(ticketList);

		// Show individual prices
		displayPrices(ticketPrices, ticketCount);
		
		// Prompt user to enter email and mobile number or cancel
		while (!exit) {
			System.out.println("Please select a choice:");
			System.out.println("1. Enter payment details");
    		System.out.println("0. Back to ticket selection");		
    		
    		choice = sc.nextInt();
    		
    		switch (choice) {
    			case 0: // Exit
    				exit = true;
    				resetSelf();
    				break;
    			case 1: // Get payment details
    				System.out.println("Please enter your name:");
    				input = sc.next();
    				
    				// Validate name, error messages are auto generated by validation function
    				if (validateName(input)) {
    					setBookerName(input); // Update name
    					
    					// Same for email
    					System.out.println("Please enter your email:");
    					input = sc.next();
    							
    					if (validateEmail(input)) {
    						setBookerEmail(input);
    						
    						// Same for mobileNo
        					System.out.println("Please enter your mobile number (no country code):");
        					input = sc.next();
        					
        					if (validateMobileNo(input)) {
        						setBookerMobileNo(input);
        						
        						// Finally, get the credit card number
        						System.out.println("Please enter your credit card number (no dashes):");
            					input = sc.next();
            					
            					if (validateCreditCard(input)) {
            						setBookerCreditCard(input.replaceAll("[^0-9]+", "")); // Remove all non-numerics;
            						
            						// We assume all payment transactions go through, and therefore we are done.
            						// Now we have to get BookingManager to finalize and raise the event.
            						// ============ END OF TICKET BOOKING ============= //
            						BookingManager.getInstance().makeBooking();
            					}
        					}
    					}
    				}
    				break; // End of case 1
    			default:
    				System.out.println("Invalid choice, please try again.");
    		}
		}
	}
	
	
	// Displays pricing information and total price
	public void displayPrices(Map<TicketType, Double> ticketPrices, Map<TicketType, Integer> ticketCount) {
		
		// Print out selected ticket prices inclusive of GST and total amount
		System.out.println("Please check your booking details below:");
		System.out.printf("%-20s%-20s%-20s%-s\n", "Item", "Unit Price", "Quantity", "Net Price");
		for (Map.Entry<TicketType, Integer> item : ticketCount.entrySet()) {
			
			// Item and quantity
			System.out.printf("%-20s%-20.2fx%-19d", item.getKey().toString() + " TICKET", ticketPrices.get(item.getKey()), item.getValue());
			
			// Net price (item price multiplied by amount of items)
			System.out.printf("%-.2f\n", ticketPrices.get(item.getKey()) * item.getValue());            			
		}
		
		// Booking fee
		System.out.printf("%-20s%-20.2fx%-19d%-.2f\n", "BOOKING FEE", SystemSettingsManager.getInstance().getPrice("BOOKING"), 1, SystemSettingsManager.getInstance().getPrice("BOOKING"));
		
		// Print net total
		System.out.println("----------------------------------------------------------------------------------");
		System.out.printf("%-60s%-.2f", "NET TOTAL (INCL. GST)", getTransaction().getTotalPrice());
	}
	
	
	// Validate user's name, check if all alphabets
	public Boolean validateName(String name) {
		char[] chars = name.toCharArray();

		if (name.length() > 50) {
			System.out.println("Sorry, we are not able to store such a long name. Please input a shorter name.");
			return false;
		}
		
	    for (char c : chars) {
	        if(!Character.isLetter(c)) {
	        	System.out.println("Your name should be purely alphabetic. Please try again.");
	            return false;
	        }
	    }

	    return true;
	}
	
	// Validate mobile number of user, check if all numeric and is 8 digits long (Assume Singapore)
	protected Boolean validateMobileNo(String mobileNo) {	
		char[] chars = mobileNo.toCharArray();

		// Check length
		if (mobileNo.length() != 8) {
			System.out.println("Please enter a valid 8 digit mobile number (no country code).");
			return false;
		}
		
	    for (char c : chars) {
	        if(!Character.isDigit(c)) {
	        	System.out.println("Your mobile number must be purely numeric. Please try again.");
	            return false;
	        }
	    }

	    return true;
	}
	
	// Checks if email address is valid
	protected Boolean validateEmail(String email) {
		// Check length
		if (email.length() > 100) {
			System.out.println("Sorry, your email is too long. Please use a shorter email.");
			return false;
		}
		
		// Match email against pattern using regex
		String pattern = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
		if(!(email.matches(pattern))) {
			System.out.println("Sorry, that email is invalid. Please try again.");
			return false;
		}
		
		return true;
	}
	
	// Checks if credit card is valid
	protected Boolean validateCreditCard(String creditCardNo) {
		
		String card = creditCardNo.replaceAll("[^0-9]+", ""); // Remove all non-numerics
        
		// Feel free to comment any of these checks away to facilitate testing
		
		// Check length
		if ((card == null) || (card.length() < 13) || (card.length() > 19)) {
			System.out.println("The credit card number you've entered is too short. Please try again.");
            return false;
        }

		// Luhn's check sees if the number is a valid credit card number (not whether it exists)
        if (!luhnCheck(card)) {
        	System.out.println("The credit card number you've entered is invalid. Please try again.");
        	return false;
        }
        
        // Check if card belongs to a reputable company
        if (!creditCardCompanyCheck(card)) {
        	System.out.println("The credit card number you've entered is invalid. Please try again.");
        	return false;
        }
        
        return true;
	}
	
	
	// Luhn's check for credit card validity
	protected Boolean luhnCheck(String cardNumber) {
		// Takes in a pure digit card number
        int digits = cardNumber.length();
        int oddOrEven = digits & 1;
        long sum = 0;
        for (int count = 0; count < digits; count++) {
            int digit = 0;
            try {
                digit = Integer.parseInt(cardNumber.charAt(count) + "");
            } catch(NumberFormatException e) {
                return false;
            }

            if (((count & 1) ^ oddOrEven) == 0) { // not
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        return (sum == 0) ? false : (sum % 10 == 0);
    }
	
	
	// Credit card company check
	protected Boolean creditCardCompanyCheck(String cardNumber) {
		List<String> cardCompanies = new ArrayList<String>();
		cardCompanies.add("^4[0-9]{12}(?:[0-9]{3})?$"); // VISA
		cardCompanies.add("^5[1-5][0-9]{14}$"); // MASTER
		cardCompanies.add("^3[47][0-9]{13}$"); // AMEX
		cardCompanies.add("^3(?:0[0-5]|[68][0-9])[0-9]{11}$"); // DINERS
		cardCompanies.add("^6(?:011|5[0-9]{2})[0-9]{12}$"); // DISCOVER
		cardCompanies.add("^(?:2131|1800|35\\d{3})\\d{11}$"); // JCB
		
		// If card number matches any major company, return true, else the card is suspicious
		for (int i = 0; i < cardCompanies.size(); i++) {
			if (cardNumber.matches(cardCompanies.get(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	
	// Update total price of booking
	public void updateTotalPrice(Map<TicketType, Double> ticketPrices, Map<TicketType, Integer> ticketCount) {
	
		double totalPrice = 0;
		
		for (Map.Entry<TicketType, Integer> item : ticketCount.entrySet()) {
			
			// Net price (ticket price multiplied by amount of items)
			totalPrice += ticketPrices.get(item.getKey()) * item.getValue();            			
		}
		
		// Booking fee
		totalPrice += SystemSettingsManager.getInstance().getPrice("BOOKING");
		
		getTransaction().setTotalPrice(totalPrice);
	}
	
	
	// Make transaction, called after transaction is approved, listen to EVENT
	public void confirmTransaction() {
		
		// Fill up booking details
		getTransaction().setTransactionID();
		getTransaction().setCreditCardNo(getBookerCreditCard());
		BookingManager.getInstance().getBooking().setTransaction(getTransaction());
		CustomerManager.getInstance().updateCustomer(getBookerName(), getBookerEmail(), getBookerMobileNo());
		
		// Update movie's total grossings
		Movie currMovie = BookingManager.getInstance().getShowtime().getMovie();
		currMovie.setGrossProfit(currMovie.getGrossProfit() + getTransaction().getTotalPrice());
		
		resetSelf();
	}
	
	
	// Self reset
	public void resetSelf() {
		setTransaction(null);
		setBookerName(null);
		setBookerMobileNo(null);
		setBookerEmail(null);
		exit = true;
	}
	
	
	// Getters
	
	public Transaction getTransaction() {return transaction;}
	public String getBookerName() {return bookerName;}
	public String getBookerMobileNo() {return bookerMobileNo;}
	public String getBookerEmail() {return bookerEmail;}
	private String getBookerCreditCard() {return bookerCreditCard;}
	
	
	// Setters
	
	public void setTransaction(Transaction transaction) {this.transaction = transaction;}
	public void setBookerName(String bookerName) {this.bookerName = bookerName;}
	public void setBookerMobileNo(String bookerMobileNo) {this.bookerMobileNo = bookerMobileNo;}
	public void setBookerEmail(String bookerEmail) {this.bookerEmail = bookerEmail;}
	private void setBookerCreditCard(String bookerCreditCard) {this.bookerCreditCard = bookerCreditCard;}
}
