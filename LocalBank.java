import java.util.ArrayList;
import java.sql.*;

/**Handles all operations that interact with the bank*/
public class LocalBank 
{
    /**The current login account number*/
    int theAccNumber = 0;
    /**The current login password*/
    int theAccPasswd = 0;
    
    /**{@code DataHandler} instance that interacts with the database*/
    public DataHandler data = new DataHandler(this);
    /**{@code LocalBankAccount} object for recording unsaved changes*/
    LocalBankAccount currentAccount = null;

    /**{@code LocalBank} constructor, creates accounts for testing*/
    public LocalBank() {
        Debug.trace( "LocalBank::<constructor>"); 
        
        //Basic
        data.createAccount(10478, 54321, 0, 0, "BasicAccount");
        //Student
        data.createAccount(24601, 12345, 0, -1200, "StudentAccount");
    }
    
    /** Creates a {@code LocalBankAccount} object that mimics the user's data<br>
     *  in the database. This enables fast modifications to the data without<br>
     *  putting a heavy load on the database.
     * @param accountType The type of account to make
     * @param id          The account's id
     * @param bal         The account's balance
     * @param over        The account's overdraft
     * @return            A {@code LocalBankAccount} object, either {@code BasicAccount}
     *                    or {@code StudentAccount}
     */
    public LocalBankAccount makeAccount(String accountType, int id, int bal, int over) {
        Debug.trace("LocalBank::makeAccount " + accountType);
        LocalBankAccount newAccount = null;
        
        switch (accountType) {
            case "BasicAccount":
                newAccount = new BasicAccount(id, bal, over);
                
            case "StudentAccount":
                newAccount = new StudentAccount(id, bal, over);
        }
        
        return newAccount;
    }

    /** Sets the entered account number
     * @param accNumber Newly entered account number
     */
    public void setAccNumber( int accNumber ) { 
        Debug.trace( "LocalBank::setAccNumber: accNumber = " + accNumber ); 

        theAccNumber = accNumber;
    }

    /** Sets the entered account password
     * @param accPasswd Newly entered account password
     */
    public void setAccPasswd( int accPasswd ) { 
        Debug.trace( "LocalBank::setAccPasswd: accPassword = " + accPasswd ); 

        theAccPasswd = accPasswd;
    }

    /** Checks whether the entered username/password are valid<br>
     *  If not valid, resets the login variables
     */
    public boolean checkValid() { 
        Debug.trace( "LocalBank::checkValid" ); 
        
        if (data.checkPassword(theAccNumber, theAccPasswd)) {
            return true;
        }
            
        //Not valid - reset everything and return false
        theAccNumber = -1;
        theAccPasswd = -1;    
        currentAccount = null;
        return false;
    }

    /** Saves new balance and resets login variables
     * 
     */
    public void logOut() {
        Debug.trace( "LocalBank::logOut" ); 
        
        data.saveAccount(currentAccount);

        theAccNumber = -1;
        theAccPasswd = -1;     
        currentAccount = null;
    }
    
    /** Withdraws money from the {@link currentAccount},<br>
     *  provided withdrawing the amount would not pass their overdraft limit
     * @param withdrawAmount Value to remove from balance
     * @return {@code true} if money withdrawn, otherwise {@code false}
     */
    public boolean withdraw( int withdrawAmount ) { 
        Debug.trace( "LocalBank::withdraw:: Amount =" + withdrawAmount ); 
        
        int balance = currentAccount.balance;
        int overdraft = currentAccount.overdraft;
        
        //If they just hit enter, don't show success message
        if (withdrawAmount <= 0) { return false; }
        
        //If there is money remaining then withdraw it, otherwise show error
        if ((balance - withdrawAmount) > overdraft) {
            currentAccount.balance -= withdrawAmount;
            return true; 
        } else { 
            return false;            
        }     
    }

    /** Increases the balance of the {@link currentAccount}
     * @param depositAmount Value to add to the balance
     * @return {@code true} if money deposited, otherwise {@code false} 
     */
    public boolean deposit( int depositAmount ) { 
        Debug.trace( "LocalBank::deposit: amount = " + depositAmount ); 
        
        if (depositAmount <= 0) {
            return false;
        } else {
            //Add amount to balance
            currentAccount.balance += depositAmount;
            return true; 
        }
    }

    /** Returns the balance of the {@link currentAccount}
     * 
     */
    public int getBalance() { 
        Debug.trace( "LocalBank::getBalance" ); 

        return currentAccount.balance;
    }
}