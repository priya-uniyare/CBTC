import java.io.*;
import java.util.HashMap;
import java.util.Map;

class Account implements Serializable {
    private String accNum;
    private String owner;
    private double balance;

    public Account(String accNum, String owner) {
        this.accNum = accNum;
        this.owner = owner;
        this.balance = 0.0;
    }

    public String getAccountNumber() {
        return accNum;
    }

    public String getOwner() {
        return owner;

    }

    public double getBalance() {
        return balance;
    }

    public boolean deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            return true;

        }
        return false;

    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

}

class Bank {
    private Map<String, Account> accounts;
    private String dataFile;

    public Bank(String dataFile)

    {
        this.accounts = new HashMap<>();
        this.dataFile = dataFile;
        loadAccounts();

    }

    public boolean createAccount(String accNum, String owner) {
        if (!accounts.containsKey(accNum)) {
            accounts.put(accNum, new Account(accNum, owner));
            saveAccounts();
            return true;

        }
        return false;
    }

    public Account getAccount(String accNum) {
        return accounts.get(accNum);
    }

    public boolean deleteAccount(String accNum)
    {
        if(accounts.containsKey(accNum))
        {
            accounts.remove(accNum);
            saveAccounts();
            return true;

        }
        return false;
    }
    public boolean transferFunds(String fromAccNum, String toAccNum, double amount) {
        Account fromAccount = getAccount(fromAccNum);
        Account toAccount = getAccount(toAccNum);
        if (fromAccount != null && toAccount != null && fromAccount.withdraw(amount)) {
            toAccount.deposit(amount);
            saveAccounts();
            return true;
        }
        return false;
    }

    private void saveAccounts() {
        // TODO Auto-generated method stub
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(accounts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAccounts() {
        // TODO Auto-generated method stub
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            accounts = (HashMap<String, Account>) ois.readObject();
        } catch (FileNotFoundException e) {
            accounts = new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        }
}
 public class Main
{
public static void main(String []args)
{
    Bank bank=new Bank("bank_data.ser");
    bank.createAccount("123","any");
    bank.createAccount("456","many");
    bank.getAccount("123").deposit(500);
    bank.transferFunds("123", "456", 200);
    System.out.println("Any's balance :"+bank.getAccount("123").getBalance());
    System.out.println("Many's balance:"+bank.getAccount("456").getBalance());
}
}