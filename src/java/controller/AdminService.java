
package controller;

import javax.ejb.EJB;
import javax.ws.rs.CookieParam;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import static utils.Utils.notNull;
import static utils.Utils.statusResponse;
import utils.ResponseString;

/**
 * REST Web Service
 * Contains admin-related methods.
 * @author Ville L
 */
@Path("AdminService")
public class AdminService {
    
    @EJB
    private UserBean uBean;

    public AdminService() {
    }
    
    // NOTE: only superadmin can make new admins, so only s/he should be able to see the button by which to grant new admin rights.
    // thus no check for admin status is needed.
    @POST
    @Path("GrantAdmin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response grantAdmin(@FormParam("makeAdmin") String alias) {

            User u = uBean.findByX("Alias", alias); 
            
        if (notNull(u)) {
        
            u.setAdmin(1); // only admins may be made like this, not superadmins
            uBean.updateDbEntry(u); // update the db with the change

            ResponseString s = new ResponseString();
            s.add("status", "madeAdmin");
            s.add("adminName", alias);
            s.add("adminStatus", u.getAdmin()+"");
            s.pack();
            return Response.ok(s.toString()).build();
        } else {
            return statusResponse("noSuchUser");
        }
        // NOTE: theoretically, the superadmin could make themselves a regular admin, but we'll assume they're not that stupid...
    } // end grantAdminRights()
    
    // gives you your own admin status
    // NOTE: storing the admin status in a cookie would mean much less db calls... could be worth figuring out how to deal with multiple cookie values
    @POST
    @Path("AdminStatus")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response adminStatus(@CookieParam("id") int ownId) {
    
        User u = uBean.findById(ownId);
        
        return statusResponse(u.getAdmin()+"");
    }
    
    // check some other user's admin status
    @GET
    @Path("CheckAdminStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAdmin(@QueryParam("checkAdmin") String alias) {
        
        // TODO: see that the user exists
        
        User u = uBean.findByX("Alias", alias);  
        
        if (notNull(u)) {
            ResponseString s = new ResponseString();
            s.add("status", "checkAdmin");
            s.add("adminName", alias);
            s.add("adminStatus", u.getAdmin()+"");
            s.pack();
            return Response.ok(s.toString()).build();
        } else {
            return statusResponse("noSuchUser");
        }
    }
    
    // NOTE: only superadmin can revoke admin rights, so only s/he should be able to see this button
    @POST
    @Path("RevokeAdmin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeAdmin(@FormParam("revokeAdmin") String alias) {
        
        // TODO: see that the user exists
        
        User u = uBean.findByX("Alias", alias);  
        
        if (notNull(u)) {
        
            u.setAdmin(0);
            uBean.updateDbEntry(u);

            ResponseString s = new ResponseString();
            s.add("status", "revokedAdmin");
            s.add("adminName", alias);
            s.add("adminStatus", u.getAdmin()+"");
            return Response.ok(s.toString()).build();
        } else {
            return statusResponse("noSuchUser");
        }
    }

    // Meant for admin operations (only admins can see this)
    @POST
    @Path("RemoveAnyUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAnyUser(@CookieParam("id") int ownId, @FormParam("userToRemove") String alias) {
        
        User u = uBean.findByX("Alias", alias);
        
        if (notNull(u)) {
                            
            int id = u.getId();                 
            int ownAdmin = uBean.findById(ownId).getAdmin(); 
                    
            if (u.getId() == ownId) { // delete own user
                
                uBean.deleteFromDb(u);
                return statusResponse("removedOwnUser");
            }
            else if (ownAdmin == 2) { // delete other user as superadmin
                
                uBean.deleteFromDb(u); 
                return statusResponse("removedOtherUser");
            } 
            else { // delete other user as regular admin (regular users cannot see this command, so a check for them is not needed)

                if (u.getAdmin() == 0) {
                
                    uBean.deleteFromDb(u);
                    return statusResponse("removedOtherUser");
                
                } else {
                
                    return statusResponse("deniedToRemoveOtherAdmin");                
                }
            } // end admin status else-if      
        } 
        else {       
            return statusResponse("noSuchUser");
        } // end nullcheck else-if
    } // end removeAnyUSer()
} // end class
