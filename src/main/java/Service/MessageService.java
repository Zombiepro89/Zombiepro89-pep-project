package Service;

import DAO.MessageDAO;
import Model.Message;

import java.util.List;
import java.util.ArrayList;

public class MessageService {

    MessageDAO messageDao;

    // When initialized, create a new interface with the database.
    public MessageService(){
        messageDao = new MessageDAO();
    }

    public List<Message> getAllMessages(){
        return messageDao.getAllMessages();
    }

    public List<Message> getAllMessagesFromUser(int account_id){
        return messageDao.getAllMessagesFromUser(account_id);
    }

    public Message getMessageByID(int message_id){
        return messageDao.getMessageByID(message_id);
    }

    public Message createMessage(Message message){
        return messageDao.insertMessage(message);
    }

    public Message updateMessageText(int message_id, String newMessage){
        return messageDao.updateMessageText(message_id, newMessage);
    }

    public Message deleteMessageByID(int message_id){
        return messageDao.deleteMessageByID(message_id);
    }

    
    
}
