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
    
    /**
     * Attempts to get all messages present in the database
     * 
     * @return A List<Message> of every message present in the database. This list is empty if request fails
     */
    public List<Message> getAllMessages(){
        List<Message> returnList = new ArrayList<Message>();

        try(Connection connection = ConnectionUtil.getConnection())
        {
            // Execute query
            String sql = "SELECT * FROM Message;";
            Statement query = connection.createStatement();
            ResultSet messageRS = query.executeQuery(sql);

            // Retrieve all messages from query and insert them into the list
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

        // Return the messages retrieved from the query
        return returnList;
    }

    /**
     * Attempts to retrieve all messages send by a given user
     * 
     * @param account_id The user to retrieve messages from
     * @return A List<Message> of all messages the user with the given account_id has sent. 
     *  Will be empty if user with account_id does not exist or request fails.
     */
    public List<Message> getAllMessagesFromUser(int account_id){
        List<Message> messages = new ArrayList<Message>();
        
        try(Connection connection = ConnectionUtil.getConnection()){
            // Ready query and execute
            String sql = "SELECT * FROM Message WHERE posted_by = ?";
            PreparedStatement query = connection.prepareStatement(sql);
            query.setInt(1, account_id);
            ResultSet messageRS = query.executeQuery();

            // Get every returned message and insert them into the list
            while(messageRS.next()){
                Message currMessage = new Message(messageRS.getInt("message_id"),
                messageRS.getInt("posted_by"), messageRS.getString("message_text"),
                messageRS.getLong("time_posted_epoch"));
                messages.add(currMessage);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        // Return the messages retrieved from the query
        return messages;
    }

    /**
     * Attempts to post message to platform
     * 
     * @param message The message to be posted
     * @return The message that was posted if successful. Returns null if post was not successfully posted.
     */
    public Message insertMessage(Message message){
        String messageText = message.getMessage_text();
        
        // If this is an invalid message, return null
        if(messageText.length() <= 0 || messageText.length() >= 255){
            return null;
        }

        // Message is, at least, valid, so attempt to insert
        try(Connection connection = ConnectionUtil.getConnection()){
            

            String sql = "INSERT INTO Message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?);";
            PreparedStatement query = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

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

    /**
     * Attempts to retrieve a message with the provided message_id
     * 
     * @param message_id The message to retrieve
     * @return The message that has the associated message_id, null if no such message exists.
     */
    public Message getMessageByID(int message_id){

        try(Connection connection = ConnectionUtil.getConnection()){
            // Ready query and execute
            String sql = "SELECT * FROM Message WHERE message_id = ?";
            PreparedStatement query = connection.prepareStatement(sql);
            query.setInt(1, message_id);
            ResultSet result = query.executeQuery();

            // Get and return the message that was retrieved, if exists
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

    /**
     * Attempts to update an existing messages text, with the provided message_id, with a new message
     * 
     * @param message_id The message to be updated
     * @param newMessage The message to replace the old one
     * @return The updated message, if successful, null otherwise.
     */
    public Message updateMessageText(int message_id, String newMessage){
        try (Connection connection = ConnectionUtil.getConnection()){
            
            // If the message is invalid, don't update
            if(newMessage.length() <= 0 || newMessage.length() >= 255){
                return null;
            }
            
            // Ready our query
            String sql = "UPDATE Message SET message_text = ? WHERE message_id = ?";
            PreparedStatement query = connection.prepareStatement(sql);
            query.setString(1, newMessage);
            query.setInt(2, message_id);

            // If a row was updated, then the message was successfully updated
            if(query.executeUpdate() > 0){
                return getMessageByID(message_id);
            }
        } 
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Message was not updated
        return null;
    }

    /**
     * Attempts to delete a message that has the given message_id
     * 
     * @param message_id The message to be deleted
     * @return The deleted message, if successful, null otherwise
     */
    public Message deleteMessageByID(int message_id){
        try(Connection connection = ConnectionUtil.getConnection()){
            Message messageToDelete = getMessageByID(message_id);
            
            // If message_id were a string, this would be scary, but here it should be fine
            String sql = "DELETE FROM Message WHERE message_id = " + message_id;
            Statement query = connection.createStatement();

            // If a row was updated, then the message was deleted successfully
            if(query.executeUpdate(sql) > 0){
                return messageToDelete;
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        // No messages were deleted
        return null;
    }
}
