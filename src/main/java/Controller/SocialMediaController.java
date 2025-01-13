package Controller;

import Model.Message;
import Service.MessageService;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
    MessageService messageService;
    
    public SocialMediaController(){
        messageService = new MessageService();
    }

    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("example-endpoint", this::exampleHandler);
        app.get("messages", this::getAllMessages);
        app.get("accounts/{account_id}/messages", this::getAllMessagesFromUser);
        app.get("messages/{message_id}", this::getMessageByID);
        app.post("messages", this::postCreateMessage);
        app.patch("messages/{message_id}", this::updateMessageText);
        app.delete("messages/{message_id}", this::deleteMessageByID);

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void exampleHandler(Context context) {
        context.json("sample text");
    }

    /**
     * Queries the database for all messages available.
     * @param context The Javalin Context object for returning the query to the site.
     */
    private void getAllMessages(Context context){
        context.json(messageService.getAllMessages());
    }

    private void getAllMessagesFromUser(Context context){
        List<Message> messages = messageService.getAllMessagesFromUser(Integer.parseInt(context.pathParam("account_id")));
        context.json(messages);
    }

    /**
     * Returns a message from the given ID
     * 
     * @param context The Javalin Context object for getting the message_id and returning the query to the site.
     */
    private void getMessageByID(Context context){
        Message message = messageService.getMessageByID(Integer.parseInt(context.pathParam("message_id")));
        
        if(message == null){
            // Even though we didn't get a message, everything is a-ok
            context.status(200);
        }
        else{
            context.json(message);
        }
    }

    /**
     * Inserts message into database and returns the message for the user to see.
     * @param context The Javalin Context object for running the post request. Also returns message as string
     */
    private void postCreateMessage(Context context) throws JsonProcessingException{
        ObjectMapper objM = new ObjectMapper();
        Message message = objM.readValue(context.body(), Message.class);
        message = messageService.createMessage(message);
        if(message!=null){
            context.json(objM.writeValueAsString(message));
        }
        else{
            context.status(400);
        }
    }

    private void updateMessageText(Context context) throws JsonProcessingException{
        int message_id = Integer.parseInt(context.pathParam("message_id"));

        // Extract the new message from the body into a temporary message object
        ObjectMapper objM = new ObjectMapper();
        Message tempMessage = objM.readValue(context.body(), Message.class);
        
        Message updatedMessage = messageService.updateMessageText(message_id, tempMessage.getMessage_text());

        if(updatedMessage != null){
            context.json(updatedMessage);
        }
        else{
            context.status(400);
        }
    }

    private void deleteMessageByID(Context context){
        Message message = messageService.deleteMessageByID(Integer.parseInt(context.pathParam("message_id")));
        
        if(message == null){
            // Even though we didn't get a message, everything is a-ok
            context.status(200);
        }
        else{
            context.json(message);
        }
    }

}