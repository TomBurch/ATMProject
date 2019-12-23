public class LocalBankAccount 
{
    public int id;
    public int balance;
    public int overdraft;
    public int interest;
    
    public LocalBankAccount(int i, int bal, int over) {
        this.id = i;
        this.balance = bal;
        this.overdraft = over;
    }
}

class BasicAccount extends LocalBankAccount {
    //BasicAccount has no overdraft
    BasicAccount(int id) {
        super(id, 0, 0);
    }
    
    BasicAccount(int id, int bal) {
        super(id, bal, 0);
    }   
    
    BasicAccount(int id, int bal, int over) {
        super(id, bal, over);
    }
}

class StudentAccount extends LocalBankAccount {
    //StudentAccount has an overdraft of £1200
    StudentAccount(int id) {
        super(id, 0, 1200);
    }
    
    StudentAccount(int id, int bal) {
        super(id, bal, 1200);
    }
    
    StudentAccount(int id, int bal, int over) {
        super(id, bal, over);
    }
}
