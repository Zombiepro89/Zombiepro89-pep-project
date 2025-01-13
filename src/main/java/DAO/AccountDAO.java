package DAO;

import java.sql.SQLException;
import java.sql.Statement;

import Model.Account;

import Util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccountDAO {
    public Account registerUser(Account newAccount){

        String username = newAccount.getUsername();
        String password = newAccount.getPassword();

        if(username.length() <= 0 || password.length() < 4){
            return null;
        }

        try(Connection connection = ConnectionUtil.getConnection()){
            String sql = "INSERT INTO Account (username, password) VALUES (?, ?)";
            PreparedStatement query = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            query.setString(1, username);
            query.setString(2, password);
            query.executeUpdate();

            ResultSet newAccountID = query.getGeneratedKeys();
            if(newAccountID.next()){
                newAccount.setAccount_id(newAccountID.getInt("account_id"));

                return newAccount;
            }

        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Account loginUser(Account accountDetails){
        try (Connection connection = ConnectionUtil.getConnection()){
            String sql = "SELECT * FROM Account WHERE username = ? AND password = ?";
            PreparedStatement query = connection.prepareStatement(sql);

            query.setString(1, accountDetails.getUsername());
            query.setString(2, accountDetails.getPassword());
            ResultSet account = query.executeQuery();

            if(account.next()){
                accountDetails.setAccount_id(account.getInt("account_id"));
                return accountDetails;
            }
        } 
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }


        return null;
    }
}
