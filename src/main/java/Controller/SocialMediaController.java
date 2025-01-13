package Controller;

// Custom Classes Imported
import Model.Message;
import Model.Account;
import Service.MessageService;
import Service.AccountService;

// External Libraries
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Routes and handles any requests sent to the platform. Leverages the AccountService and MessageService 
 * classes for accomplish its task.
 */
public class SocialMediaController {
    AccountService accountService;
    MessageService messageService;
    
    public SocialMediaController(){
        accountService = new AccountService();
        messageService = new MessageService();
    }

    
    /**
     * Initializes the paths for the platform
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();

        //Account related handlers
        app.post("register", this::registerUser);
        app.post("login", this::loginUser);

        // Message related handlers
        app.get("messages", this::getAllMessages);
        app.get("accounts/{account_id}/messages", this::getAllMessagesFromUser);
        app.get("messages/{message_id}", this::getMessageByID);
        app.post("messages", this::postCreateMessage);
        app.patch("messages/{message_id}", this::updateMessageText);
        app.delete("messages/{message_id}", this::deleteMessageByID);

        return app;
    }


    /**
     * Attempts to register user using the information in the body of the request.
     * Sends a HTTP 200 status code if this is done successfully
     * Sends a HTTP 400 status code if the user details are invalid.
     * 
     * For an account to be valid, the username must be at least 1 character long
     * and the password must be at least 4 characters long.
     * 
     * @param context The Javalin Context object that contains the account information
     * @throws JsonProcessingException Thrown if body is not of expected format.
     */
    private void registerUser(Context context) throws JsonProcessingException{
        // Extract account information from request body
        ObjectMapper objM = new ObjectMapper();
        Account newAccount = objM.readValue(context.body(), Account.class);

        // Attempt to register user in the database
        newAccount = accountService.registerUser(newAccount);

        if(newAccount != null){
            // User has be registered, return account info
            context.json(newAccount);
        }
        else{
            // Something went wrong, send the cooresponding status code
            context.status(400);
        }
    }


    /**
     * Attempts to login the user given the account details in the body of the request.
     * 
     * Sends a HTTP 200 status code if login was successful
     * Sends a HTTP 401 status code if details are invaild
     * 
     * @param context The Javalin context that contains the login details
     * @throws JsonProcessingException Thrown if body is not expected format
     */
    private void loginUser(Context context) throws JsonProcessingException{
        // Extract account information from request body
        ObjectMapper objM = new ObjectMapper();
        Account loginDetails = objM.readValue(context.body(), Account.class);

        Account returnedAccount = accountService.loginUser(loginDetails);

        if(returnedAccount != null){
            // Details were valid, log user in
            context.json(returnedAccount);
        }
        else{
            // Invalid details were entered
            context.status(401);
        }
    }


    /**
     * Queries the database for all messages available.
     * @param context The Javalin Context object for returning the query to the site.
     */
    private void getAllMessages(Context context){
        context.json(messageService.getAllMessages());
    }


    /**
     * Queries the database for all messages send by this user
     * @param context The Javalin Context object for returning the query to the site.
     */
    private void getAllMessagesFromUser(Context context){
        List<Message> messages = messageService.getAllMessagesFromUser(
            Integer.parseInt(context.pathParam("account_id")));
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
     * 
     * Sends a HTTP 200 status code if message was created
     * Sends a HTTP 400 status code if message was invalid
     * 
     * For a message to be valid, the message_text must not be blank and must be under 255 characters and
     * the posted_by variable must match an existing account_id.
     * 
     * @param context The Javalin Context object for running the post request. Also returns message as string
     * @throws JsonProcessingException Thrown if body is not in expected format
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


    /**
     * Attempts to update a message given the message_id with the body of the request
     * 
     * Sends a HTTP 200 status code if update was successful
     * Sends a HTTP 400 status code if update failed
     * 
     * For a message to be updated, the message_id in the URL must match an existing message
     * and the message_text in the body must not be blank and must be under 255 characters.
     * 
     * @param context The Javalin Context object that queries the database and returns the updated message
     * @throws JsonProcessingException Thrown if body is not in expected format
     */
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

    /**
     * Attempts to delete a message with the message_id provided in the URL. Responds with deleted message.
     * 
     * Sends a HTTP 200 status code regardless of if a message is deleted for not.
     * 
     * @param context The Javalin Context object to retrieve the message_id and return the result of the query
     */
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