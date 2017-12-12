
package controller;

import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import static utils.Utils.notNull;
import static utils.Utils.statusResponse;
import utils.ResponseString;
import static utils.Validation.validUser;

/**
 * REST Web Service.
 * Contains login / signup-related methods.
 * @author Ville L
 */
@Path("LoginService")
public class LoginService {

    @EJB
    private UserBean uBean;
    
    public LoginService() {
    }
    
    // get all entries from the 'User' table
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getAllUsers() {
        
        return uBean.getAllUsers();
    }
    
    // fetch form data from the signup form and try to make a new user with it
    @POST
    @Path("SignUp")
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON) 
    public Response signUp(
            @FormParam("email") String email, 
            @FormParam("username") String name, 
            @FormParam("password") String pw, 
            @FormParam("password2") String pw2) {
             
        if (!validUser(name, email, pw, pw2)) { // method from utils.Validation.java
        
            return statusResponse("invalidUser");
        }
        else if (notNull(uBean.findByX("Alias", name))) { // username taken

            return statusResponse("aliasTaken");
        } 
        else if (notNull(uBean.findByX("Email", email))) { // email taken
        
            return statusResponse("emailTaken");
        } 
        else { // email and username are both available and every field is valid       
            User u = new User();
            u.setEmail(email);
            u.setAlias(name);
            u.setPw(pw);
            u.setAdmin(0); // 0 by default; 0 = regular user, 1 = admin, 2 = superadmin. (admins can only be added manually through MariaDB)
            uBean.insertToDb(u);
            
            ResponseString s = new ResponseString();
            s.add("status", "loggedIn");
            s.add("id", uBean.findByX("Alias", name).getId() + ""); // convert it into a String...
            s.add("email", email);
            s.add("alias", name);
            s.add("admin", "0");
            s.pack();
            return Response.ok(s.toString()).build();
            
            // TODO: on the client, since you are now registered and logged in, alter the login/register page to contain only 'logout' button
        }      
    } // end signUp()
    
    // fetch form data from the login form and try to log in with an existing user
    @POST
    @Path("LogIn")
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON) 
    public Response logIn(@FormParam("loginUsername") String name, @FormParam("loginPassword") String pw) {
    
        if (!validUser(name, pw)) {
        
            return statusResponse("invalidUser");
        }
        
        User u = uBean.findByX("Alias", name); // check user existence by name
        
        if (notNull(u) && u.getPw().equals(pw)) {
            
            // TODO: since you are now logged in, alter the login/register page to contain only 'logout' button
            
            ResponseString s = new ResponseString();
            s.add("status", "loggedIn");
            s.add("id", u.getId()+"");
            s.add("email", u.getEmail());
            s.add("alias", name);
            s.add("admin", u.getAdmin()+"");
            s.pack();
            return Response.ok(s.toString()).build();
        } 
        else if (notNull(u) && !(u.getPw().equals(pw))) {
        
            return statusResponse("wrongPw"); 
        } 
        else {    
             return statusResponse("wrongUsername");       
        }
    } // end logIn()  
} // end class
