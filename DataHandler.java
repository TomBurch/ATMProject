import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**Handles all operations that require access to the database
 * 
 */
public class DataHandler
{
    /**Link to {@code LocalBank}*/
    public LocalBank localBank; 
    
    /**Connection to the database*/ 
    Connection conn = null; 
    /**Statement used to execute SQL*/
    Statement stmt = null; 
    /**SQL string used by the statement*/
    String sql = ""; 
    
    /** {@code DataHandler} constructor, links instance back to {@code LocalBank}
     * 
     * @param parent The {@code LocalBank} instance to link to
     */
    public DataHandler(LocalBank parent) {
        localBank = parent;
    }
    
    /** Creates a new account in the database, used to setup a test but could be extended to<br>
    *   an admin system for creating new accounts
    *   
    *  @param  id              Account ID
    *  @param  password        Account password
    *  @param  balance         Starting balance
    *  @param  overdraft       Overdraft limit
    *  @throws ConstraintError If there is an existing account with that id
    */
    public void createAccount(int id, int password, int balance, int overdraft,
                              String accountType) {
        Debug.trace("DataHandler::createAccount");
        try {
            //Connect to database
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:atm.db");
            
            sql = "INSERT INTO bank(id, balance, overdraft, password, salt," +
                    "accountType, datalocked) VALUES (?,?,?,?,?,?,?)";
            
            //PreparedStatement protects against SQL injections
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            //Hash password
            byte[] salt = getSalt();
            String hashPass = hashPassword(password, salt);
            String strSalt = byteToHex(salt);
            
            //Execute INSERT
            pstmt.setInt(1, id);
            pstmt.setInt(2, balance);
            pstmt.setInt(3, overdraft);
            pstmt.setString(4, hashPass);
            pstmt.setString(5, strSalt);
            pstmt.setString(6, accountType);
            pstmt.setBoolean(7, false);
            pstmt.executeUpdate();
            
            Debug.trace("DataHandler::createAccount::Created account " + id);
            
            //Close connection to database
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            String error = e.toString();
            if (error.contains("CONSTRAINT")) { 
                //Attempted to make non-unique account
                Debug.trace("DataHandler::createAccount::Account " + id + " already exists");   
            } else { 
                //Unexpected error
                Debug.trace("DataHandler::createAccount::DBError::" + error);
            }
        }
    }
    
    /** Checks entered password against one stored in the database<br>
     *  If equal, creates new {@code CurrentAccount} object with {@link LocalBank#makeAccount}
     * 
     * @param i     ID to check password against
     * @param pass  Password to check
     * @return      {@code true} if passwords match, otherwise {@code false}
     */
    public boolean checkPassword(int i, int pass) {    
        Debug.trace("DataHandler::checkPassword");
        
        try { 
            //Connect to database
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:atm.db");
            Statement stmt = conn.createStatement();
       
            //Select result with target ID
            String sql = "SELECT * FROM bank WHERE id = " + i;
            ResultSet rs = stmt.executeQuery(sql);
            rs.next(); //Selects 1st result
            
            //Retrieve hashed password from database
            String password = rs.getString("password");
            String salt = rs.getString("salt");
            //Convert salt to byte form
            byte[] byteSalt = hexToByte(salt);
            
            //Hash entered password using same salt
            String hashPass = hashPassword(pass, byteSalt);
            
            //Check entered password against one in database
            if (hashPass.equals(password)) {
                //Variables needed for account object
                int id = rs.getInt("id");
                int balance = rs.getInt("balance");
                int overdraft = rs.getInt("overdraft");
                String accountType = rs.getString("accountType");
                
                //Check if account is in use
                boolean datalocked = rs.getBoolean("datalocked");
                Debug.trace("--------------");
                if (datalocked == true) { 
                    throw new Exception("Account data is locked"); 
                } else {
                   //Lock the account to stop other sources updating it, necessary for
                   //multi-access systems.
                   sql = "UPDATE bank SET datalocked = true WHERE id = " + i;
                   stmt.executeUpdate(sql);
                }
                                   
                //Create new account object with same properties as database
                localBank.currentAccount = localBank.makeAccount(accountType, id, balance, overdraft);
               
                //Close connection to database
                rs.close();
                stmt.close();
                conn.close();
                return true;
            }
        } catch (Exception e) {
            String error = e.toString();
            Debug.trace("DataHandler::checkPassword DBerror::" + error);
        } 
        return false;
    }
    
    /** Saves the updated balance to the database
     * 
     * @param account The account with unsaved data
     */
    public void saveAccount(LocalBankAccount account) {
        Debug.trace("DataHandler::saveAccount");
        
        if (account == null) {
            Debug.trace("DataHandler::saveAccount::No account to save");
            return;
        }
        
        try {
            //Connect to database
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:atm.db");
            stmt = conn.createStatement();
           
            //Update values in database under account id
            int id = account.id;
            int balance = account.balance;
            
            sql = "UPDATE bank SET balance = " + balance +
                " WHERE id = " + id;
            stmt.executeUpdate(sql);
            
            //Unlock the account data
            sql = "UPDATE bank SET datalocked = false WHERE id = " + id;
            stmt.executeUpdate(sql);
            
            //Close connection to database
            stmt.close();
            conn.close();
        } catch (Exception e) {
            String error = e.toString();
            Debug.trace("DataHandler::saveAccount DBerror::" + error);
        }
    }
    
    /** Hashes and salts passwords using MD5
     * 
     * @param password  Password that is being hashed
     * @param salt      Salt to be used in hash
     * @return          A hexadecimal string, the hashed password
     * @throws          NoSuchAlgorithmException
     */
    public String hashPassword(int pass, byte[] salt) {
        Debug.trace("DataHandler::hashPassword");
        
        try {
            String password = Integer.toString(pass);
            String generatedPassword = null;
            
            //Chosen hash algorithm, should swap for more secure one
            MessageDigest md = MessageDigest.getInstance("md5");
            //Update the digest algorithm with the salt 
            md.update(salt);
            //Hash the password using the digest algorithm
            byte[] bytes = md.digest(password.getBytes());
            
            //Convert the hash to hexadecimal
            generatedPassword = byteToHex(bytes);
            
            return generatedPassword;
        } catch (NoSuchAlgorithmException e) {
            Debug.trace(e.toString());
            System.exit(0);
        }
        return null;
    }
    
    /** Creates a random 16 byte salt to be used in hashing
     * 
     * @return A 16 byte salt
     */
    public byte[] getSalt() {
        Debug.trace("DataHandler::getSalt");
        
        try {
        //Secure random number generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        //Create a random salt
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        
        return salt;
        } catch (NoSuchAlgorithmException e) {
            Debug.trace(e.toString());
            System.exit(0);
        } catch (NoSuchProviderException e) {
            Debug.trace(e.toString());
            System.exit(0);
        }
        return null;
    }
    
    /** Converts decimal bytes into hexadecimal string
     * 
     * @param bytes The bytes to be converted
     * @return      The bytes converted into a hexadecimal string;
     * 
     * @author Lokesh Gupta
     * @see <a href = https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/> Source
     */
    public String byteToHex(byte[] bytes) {
        Debug.trace("DataHandler::byteToHex");
        
        String hex = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        hex = sb.toString();
        
        return hex;
    }
    
    /**Converts hexadecimal string into decimal bytes
     * 
     * @param hex The string to be converted
     * @return    The string converted into decimal bytes
     * 
     * @author Samual Sam
     * @see <a href = https://www.tutorialspoint.com/convert-hex-string-to-byte-array-in-java> Source
     */
    public byte[] hexToByte(String hex) {
        Debug.trace("DataHandler::hexToByte");
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i++) {
            int index = i * 2;
            try {
                int j = Integer.parseInt(hex.substring(index, index + 2), 16);
                bytes[i] = (byte) j;
            } catch (Exception e){
                Debug.trace(e.toString());
                break;
            }
        }
        
        return bytes;
    }
}