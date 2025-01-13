package DAO;

// Our custom classes imported
import Util.ConnectionUtil;
import Model.Message;

// Java libraries imported
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MessageDAO {
    

    public List<Message> getAllMessages(){
        List<Message> returnList = new ArrayList<Message>();

        try(Connection connection = ConnectionUtil.getConnection())
        {
            String sql = "SELECT * FROM Message;";
            PreparedStatement query = connection.prepareStatement(sql);
            ResultSet messageRS = query.executeQuery();

            while(messageRS.next()){
                Message currMessage = new Message(messageRS.getInt("message_id"),
                    messageRS.getInt("posted_by"), messageRS.getString("message_text"),
                    messageRS.getLong("time_posted_epoch"));
                
                returnList.add(currMessage);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        return returnList;
    }

    public List<Message> getAllMessagesFromUser(int account_id){
        List<Message> messages = new ArrayList<Message>();
        
        try(Connection connection = ConnectionUtil.getConnection()){
            String sql = "SELECT * FROM Message WHERE posted_by = ?";
            PreparedStatement query = connection.prepareStatement(sql);

            query.setInt(1, account_id);

            ResultSet messageRS = query.executeQuery();
            while(messageRS.next()){
                Message currMessage = new Message(messageRS.getInt("message_id"),
                messageRS.getInt("posted_by"), messageRS.getString("message_text"),
                messageRS.getLong("time_posted_epoch"));
                messages.add(currMessage);
            }

            return messages;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }


        return null;
    }

    public Message insertMessage(Message message){
        try(Connection connection = ConnectionUtil.getConnection()){
            String messageText = message.getMessage_text();
            
            // If this is an invalid message, return null
            if(messageText.length() <= 0 || messageText.length() >= 255){
                return null;
            }


            String sql = "INSERT INTO Message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?);";
            PreparedStatement query = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            System.out.println(message.getMessage_id());

            // Insert query parameters and execute
            query.setInt(1, message.getPosted_by());
            query.setString(2, messageText);
            query.setLong(3, message.getTime_posted_epoch());
            query.executeUpdate();

            // Get the generated message_id
            ResultSet result = query.getGeneratedKeys();
            if(result.next()){
                // Insert the message_id into the object and return it
                message.setMessage_id(result.getInt("message_id"));
                return message;
            }
            
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Message getMessageByID(int message_id){

        try(Connection connection = ConnectionUtil.getConnection()){
            String sql = "SELECT * FROM Message WHERE message_id = ?";
            PreparedStatement query = connection.prepareStatement(sql);

            query.setInt(1, message_id);
            ResultSet result = query.executeQuery();

            if(result.next()){
                return new Message(result.getInt("message_id"), result.getInt("posted_by"),
                result.getString("message_text"), result.getLong("time_posted_epoch"));
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Message updateMessageText(int message_id, String newMessage){
        try (Connection connection = ConnectionUtil.getConnection()){
            
            // If the message is invalid, don't update
            if(newMessage.length() <= 0 || newMessage.length() >= 255){
                return null;
            }
            
            String sql = "UPDATE Message SET message_text = ? WHERE message_id = ?";
            PreparedStatement query = connection.prepareStatement(sql);

            query.setString(1, newMessage);
            query.setInt(2, message_id);

            if(query.executeUpdate() > 0){
                return getMessageByID(message_id);
            }

        } 
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Message deleteMessageByID(int message_id){
        try(Connection connection = ConnectionUtil.getConnection()){
            Message messageToDelete = getMessageByID(message_id);
            
            // If message_id were a string, this would be scary, but here it should be fine
            String sql = "DELETE FROM Message WHERE message_id = " + message_id;
            Statement query = connection.createStatement();

            if(query.executeUpdate(sql) > 0){
                return messageToDelete;
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        return null;
    }
}
