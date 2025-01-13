package Service;

// Imports
import DAO.AccountDAO;
import Model.Account;

public class AccountService {
    AccountDAO accountDao;

    public AccountService(){
        accountDao = new AccountDAO();
    }

    public Account registerUser(Account newAccount){
        return accountDao.registerUser(newAccount);
    }

    public Account loginUser(Account loginDetails){
        return accountDao.loginUser(loginDetails);
    }
}
