
package controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import model.Comp;
import static utils.Utils.statusResponse;
import utils.ResponseString;
import static utils.Utils.isEmpty;
import static utils.Utils.lengthOver;
import static utils.Validation.validComp;

/**
 * REST Web Service
 * Contains all the methods that are related to compositions.
 * @author Ville L
 */
@Path("CompService")
public class CompService {
    
    @EJB
    private CompBean cBean;
    
    @EJB
    private CommentBean comBean;
    
    @EJB
    private UserBean uBean;

    public CompService() {
    }
    
    // create new composition (based on form data)
    @POST
    @Path("AddComp")
    //@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON) 
    public Response addComp(
            @FormParam("title") String title, 
            @FormParam("author") String author, 
            @FormParam("length") int length, 
            @FormParam("year") int year,
            @FormParam("diff") int diff, 
            @FormParam("pages") int pages, 
            @FormParam("video") String video, 
            @FormParam("sheet") String sheet,
            @CookieParam("id") int adderId
            ) {

        if (!validComp(title, author, length, year, diff, pages, video, sheet)) {
        
            return statusResponse("invalidComp");
        }
          
        // TODO: check for duplicate Comp entries... what should be the criteria ??
       
        try {
            Date addTime = new Date(System.currentTimeMillis()); // server time when it should be client... meh, who cares.    

            Comp c = new Comp();
            c.setTitle(title);
            c.setAuthor(author);
            c.setLength(length);
            c.setYear(year);
            c.setDiff(diff);
            c.setPages(pages);
            c.setVideo(video);
            c.setSheet(sheet);
            c.setAddtime(addTime);
            c.setAdderidUser(uBean.findById(adderId));
            c.setComms(0);
            System.out.println("cBean: " + cBean.toString());
            System.out.println("c : " + c.toString());
            cBean.insertToDb(c);

            return statusResponse("addedComp"); // NOTE: more may need to be returned, depending on what we want to do after adding the composition
        } catch (Exception e) {
            e.printStackTrace();
            return statusResponse("failedToAddComp");
        }
    } // end addComp()
    
    // called when you click on the difficulty tab to display a list of compositions
    @POST
    @Path("GetCompsByDiff")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response getCompsByDiff(@QueryParam("diff") int diff) {
        
        // NOTE: the value of diff is always valid, due to how it's obtained
        
        List<Comp> comps = cBean.findAllByIntX("Diff", diff);
       
        try {
            return Response.ok(comps).build();   
        } catch (Exception e) {
            return statusResponse("failedToGetComps");
        }
    } // end getCompsByDiff()
    
    // used in a double-fetch when clicking on a composition in the larger list view
    @POST
    @Path("GetCompById")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response getCompById(@QueryParam("id") int compId) {
        
        
        try {
            Comp c = cBean.findByIntX("Id", compId);

            ResponseString s = new ResponseString();
            s.add("status", "gotCompById");
            s.add("title", c.getTitle());
            s.add("author", c.getAuthor());
            s.add("length", c.getLength()+"");
            s.add("year", c.getYear()+"");
            s.add("diff", c.getDiff()+"");
            s.add("pages", c.getPages()+"");
            s.add("video", c.getVideo());
            s.add("sheet", c.getSheet());
            s.add("addTime", c.getAddtime()+"");
            s.add("adderId", c.getAdderidUser().getId()+"");
            s.add("comms", c.getComms()+"");
            s.pack();
            return Response.ok(s.toString()).build();  
        } catch (Exception e) {
            
            return statusResponse("failedToGetComp");
        }
    } // end getCompById()
    
    // used for getting your own compositions
    @POST
    @Path("GetCompsByOwnId")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response getCompsByOwnId(@CookieParam("id") int adderId) {
        
        List<Comp> comps = cBean.findAllByIntX("AdderId", adderId);
        
        try {
            return Response.ok(comps).build();   
        } catch (Exception e) {
            return statusResponse("failedToGetComps");
        } 
    } // end getCompsById()
    
    // TODO: there is an issue with removal due to the Likes and Favorite tables referencing the Comp table; fix this asap!
    @POST
    @Path("DeleteComp")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response deleteComp(@CookieParam("id") int ownId, @QueryParam("id") int userid) { // the id comes from clicking on the composition to delete
        
        try {
            Comp c = cBean.findByIntX("Id", userid);
            cBean.deleteFromDb(c);

            return statusResponse("deletedComp");
        } catch (Exception e) {
            
            return statusResponse("failedToDeleteComp");
        } 
        // we could display stats after deletion
    } // end removeComp()
    
    // search through all compositions by their name (alphabetically)
    @POST
    @Path("TitleSearch")
    @Produces(MediaType.APPLICATION_JSON)
    public Response titleSearch(@FormParam("strToSearch") String str) {
        
        // this is a very crude and costly operation that quickly gets unworkable...
        // we can fine-tune it if we have the time
        
        if (isEmpty(str)) { // empty or contains pure whitespace
            
            return statusResponse("emptySearchString"); // unnecessary if we use js on client to prevent the sending of empty searches
        }
        
        List<Comp> comps = cBean.getAllComps(); // double overhead because we fetch all comps first w/out conditions... an actual db query with a stricter argument could be used instead
        ArrayList<Comp> resultComps = new ArrayList<>();
        int length = comps.size();
        
        for (int i = 0; i < length; i++) {
            
            if (comps.get(i).getTitle().startsWith(str)) {
                
                resultComps.add(comps.get(i));
            }       
        }
                
         try {
            return Response.ok(resultComps).build();   
        } catch (Exception e) {
            return statusResponse("searchFailed");
        }
    } // end titleSearch()

    // There are a million billion stats that it's possible to change... This method should take care of all of them
    // NOTE: this may not work as I thought... how will the statName argument be sent multiple times if you choose to edit 
    // multiple fields at once ??
    @POST
    @Path("EditComp")
    @Produces(MediaType.APPLICATION_JSON)
    public Response editComp(@CookieParam("id") int ownId, @QueryParam("id") int compId, @QueryParam("newStat") String statName, @QueryParam("newVal") String statValue) {
        
        // TODO: remove the overlap between utils.Validation.validComp() and this validation...
             
        // TODO: check credentials (admin / adder ?)
        
        Comp c = cBean.findByIntX("Id", compId); 
        String status = "noChange";
        
        switch (statName) {
        
            case "title":
                
                if (!lengthOver(statValue, 50) && !isEmpty(statValue)) {
                
                    c.setTitle(statValue);
                    status = "changedTitle";
                }
                break;
                
            case "author":
                
                if (!lengthOver(statValue, 50) && !isEmpty(statValue)) {
                    c.setAuthor(statValue);
                    status = "changedAuthor";
                }
                break;
                
            case "length":
                
                int length = Integer.parseInt(statValue);
                
                if (length > 0 && length < 36001) {
                    c.setLength(length);
                    status = "changedLength";
                }
                break;
                
            case "year":
                
                int year = Integer.parseInt(statValue);
                
                if (year > 0 && year < 10000) {
                    c.setYear(year);
                    status = "changedYear";
                }
                break;            
            
            case "diff":
                
                int diff = Integer.parseInt(statValue);
                
                if (diff == 0 || diff == 1 || diff == 2) {
                    c.setDiff(diff);
                    status = "changedDiff";
                }
                break;
                
            case "pages":
                
                int pages = Integer.parseInt(statValue);
                
                if (pages >= 0 && pages < 21) {
                    c.setPages(pages);
                    status = "changedPages";
                }
                break;            
            
            case "video":
                
                if (statValue.matches("^https\\:\\/\\/www\\.youtube\\.com\\/\\S+$")) {              
                    c.setVideo(statValue);
                    status = "changedVideo";
                }
                break;  
                
            case "sheet":
                
                if (!isEmpty(statValue)) {
                    c.setSheet(statValue);
                    status = "changedSheet";
                }
                break;               
        }
        
        cBean.updateDbEntry(c);
        
        ResponseString s = new ResponseString();
        s.add("status", status);
        s.add("newValue", statValue);         
        s.pack();
        return Response.ok(s.toString()).build();
        // TODO: send a msg to the user that they've changed the stat
    } // end changeStat()
} // end class
