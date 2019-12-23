
/** The model represents all the actual content and functionality of the app<br<
 *  For the ATM, it keeps track of the information showin in the display<br>
 *  (the two message boxes), and the interaction with the bank, and executes<br>
 *  commands provided by the controller (and tells the view to update when<br>
 *  something changes)
 */
public class Model 
{
    //States the ATM can take
    final String ACCOUNT_NO = "account_no";
    final String PASSWORD = "password";
    final String LOGGED_IN = "logged_in";
    final String WITHDRAW = "withdraw";
    final String DEPOSIT = "deposit";

    // variables representing the ATM model
    String state = ACCOUNT_NO;      // the state it is currently in
    int  number = 0;                // current number displayed in GUI (as a number, not a string)
    String display1 = null;         // The contents of the Message 1 box (a single line)
    String display2 = null;         // The contents of the Message 2 box (may be multiple lines)

    // The ATM talks to a bank, represented by the LocalBank object.
    LocalBank  bank = new LocalBank();

    // The other parts of the model-view-controller setup
    public View view;
    public Controller controller;

    // when we create a model, we set the messages to something useful
    public Model() {
        Debug.trace("Model::<constructor>");        
        initialise("Welcome to the ATM"); 
    }
    
    // set state to ACCOUNT_NO, number to zero, and display message provided as argument
    // and standard instruction message
    public void initialise(String message) {
        setState(ACCOUNT_NO);
        number = 0;
        display1 = message; 
        display2 =  "Enter your account number\n" +
        "Followed by \"ENT\"";
    }

    /**This is how the Controller talks to the Model - it calls this method<br>
     * to say that a button in the GUI has been pressed
     */
    public void process( String button ) {
        Debug.trace("Model::process:: State = " + state + " button = " + button );
        
        Boolean handled;
        
        handled = tryNumbers(button);
        if (!handled) {
            handled = tryCommands(button);
            if (! handled) {
                // unknown button, or invalid for this state - reset everything
                Debug.trace("Model::process: Unhandled button, re-initialising");
                // go back to initial state
                initialise("Invalid command");
            }
        }
        display();  // update the GUI
    }

    /** Handles all input for the NumPad buttons
     * 
     * @param button The text of the button that was pressed
     * @return {@code false} if not a NumPad button
     */
    public Boolean tryNumbers( String button ) {
        switch ( button ) {
            case "1" : case "2" : case "3" : case "4" : case "5" :
            case "6" : case "7" : case "8" : case "9" : case "0" :
                switch (state) {
                    case WITHDRAW: case DEPOSIT: //Add the value, rather than append
                        int amount = Integer.parseInt(button);
                        number = number + amount;
                        display1 = "£" + number;
                        return true;
                    default: 
                        char c = button.charAt(0);
                        number = number * 10 + c-'0';
                        display1 = "" + number;
                        return true;
                }
                                    
            case "CLR" :
                number = 0;
                display1 = "";
                return true;
                    
            case "ENT" :
                switch ( state ) {
                    case ACCOUNT_NO:
                        //Store the entered account number
                        bank.setAccNumber( number );
                        number = 0;
                        //Change to waiting for password entry
                        setState(PASSWORD);
                        
                        //Modify the display
                        display1 = "";
                        display2 = "Now enter your password\n" +
                        "Followed by \"ENT\"";
                        break;
                        
                    case PASSWORD:
                        //Store the entered password
                        bank.setAccPasswd( number );
                        number = 0;
                        //Clear the display
                        display1 = "";
                        
                        //Check entered username/password, if valid then log in
                        if ( bank.checkValid() )
                        {
                            String d2 = "Accepted" +
                                        "\nChoose the transaction that you require";
                            loadMenu(null, d2);
                        } else {
                            bank.logOut();
                            initialise("Unknown account/password");
                        }
                        break;
                       
                    case WITHDRAW: {
                        String d2;
                        if ( bank.withdraw( number ) ) {
                            d2 = "Successfully withdrawn: £" + number +
                                 "\nChoose the transaction that you require";
                        } else {
                            d2 = "You do not have sufficient funds";
                        }
                        loadMenu(null, d2);
                        break;
                    }
                   
                    case DEPOSIT: {
                        String d2;
                        if ( bank.deposit( number ) ) {
                            d2 = "Successfully deposited: £" + number +
                                 "\nChoose the transaction that you require";
                        } else {
                            d2 = "Cannot deposit that amount";
                        }
                        loadMenu(null, d2);
                        break;
                    }
            }    
            return true;
        }
        return false;
    }
    
    /** Handles most buttons on the left/right option panels
     * 
     * @param button The text of the button that was pressed
     * @return {@code false} if the button is not on option panel, i.e. not a valid button
     */
    public Boolean tryCommands(String button) {        
        if (state.equals(LOGGED_IN)) {
            switch ( button ) {
                case "W/D" : //Withdraw
                    setState(WITHDRAW);
                    display1 = "";
                    display2 = "Enter amount to withdraw, then press \"ENT\""+
                               "\nPress \"EXIT\" to return";
                    number = 0;
                    view.updateOptions("10", "20", "30", "40", "60", "80", "100", "EXIT");
                    break;
                    
                case "Bal" : // Balance
                    number = 0;
                    display2 = "Your balance is: £" + bank.getBalance() +
                               "\nChoose the transaction that you require";
                    break;
                    
                case "Dep" : // Deposit
                    setState(DEPOSIT);
                    display1 = "";
                    display2 = "Enter amount to deposit, then press \"ENT\""+
                               "\nPress \"EXIT\" to return";
                    number = 0;
                    view.updateOptions("10", "20", "30", "40", "60", "80", "100", "EXIT");
                    break;
                    
                case "Pass" :
                    Debug.trace("CHANGE PASSWORD");
                    
                case "EXIT" : // Exit
                    setState(ACCOUNT_NO);
                    //Reset the display
                    number = 0;
                    display2 = "Welcome: Enter your account number";
                    view.updateOptions("", "", "", "", "", "", "", "");
                    //Log out
                    bank.logOut();
                    break;
                    
                default:
                    Debug.trace("Model::tryCommands:: Unknown button - " + button);
                    return false;
            }
            return true;
        } else if (state.equals(DEPOSIT) || state.equals(WITHDRAW)) {
            switch (button) {
                case "10" : case "20"  : case "30"  : case "40" : case "60" :
                case "80" : case "100" :
                    int amount = Integer.parseInt(button);
                    number = number + amount;
                    display1 = "£" + number;
                    break;
                    
                case "EXIT" : //EXIT
                    loadMenu(null, null);
                    break;
                    
                default:
                    Debug.trace("Model::tryCommands:: Unknown button - " + button);
                    return false;        
            } 
            return true;  
        } else {
            return false;
        }    
    }
    
    /** Sets the state, mainly used to output a debug message
     * 
     * @param newState State to change to
     */
    public void setState(String newState) {
        if ( !state.equals(newState) ) {
            state = newState;
            Debug.trace("Model::setState:: New state = " + state);
        }
    }
    
    /** Logs in or returns to the menu
     * 
     * @param d1 The text for {@code display1}
     * @param d2 The text for {@code display2}
     */
    public void loadMenu(String d1, String d2) {
        Debug.trace("Model::loadMenu");
        
        setState(LOGGED_IN);
        number = 0;
        //If d1, display that, otherwise leave blank
        display1 = d1 != null ? d1 : "";
        //If d2, display that, otherwise display default text
        display2 = d2 != null ? d2 : "Choose the transaction that you require";
        view.updateOptions("W/D", "Dep", "Bal", "", "Pass", "", "", "EXIT");
    }
    
    /** This is where the Model talks to the View, by calling the View's update method<br>
     *  The view will call back to the model to get new information to display on the screen
     */
    public void display() {
        Debug.trace("Model::display");
        
        view.update();
    }
}