
package controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import model.Comp;
import static utils.Utils.notNull;
import static utils.Utils.setResponseStatus;
import static utils.Utils.putJson;
import org.json.JSONObject;
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

    public CompService() {
    }
    
    // create new composition (based on form data)
    @POST
    @Path("AddComp")
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON) 
    public JSONObject addComp(
            @FormParam("title") String title, 
            @FormParam("author") String author, 
            @FormParam("length") int length, 
            @FormParam("year") int year,
            @FormParam("diff") int diff, 
            @FormParam("pages") int pages, 
            @FormParam("video") String video, 
            @FormParam("sheet") String sheet
            ) {
    
        if (!validComp(title, author, length, year, diff, pages, video, sheet)) {
        
            return setResponseStatus("invalidComp");
        }
          
        // TODO: check for duplicate entries... what should be the criteria ??
        
        int adderId = 99; // PLACEHOLDER (needs to come from the request header)
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
        c.setAdderid(adderId);
        c.setComms(0);
        cBean.insertToDb(c);

        return setResponseStatus("addedComp"); // NOTE: more may need to be returned, depending on what we want to do after adding the composition
    } // end addComp()
    
    // called when you click on the difficulty tab to display a list of compositions
    @POST
    @Path("GetCompsByDiff")
    @Produces(MediaType.APPLICATION_JSON) 
    public JSONObject getCompsByDiff(@QueryParam("diff") int diff) {
        
        // NOTE: the value of diff is always valid, due to how it's obtained
        
        List<Comp> comps = cBean.findAllByIntX("Diff", diff);
       
        JSONObject j =  new JSONObject();
        putJson(j, "status", "gotCompsByDiff");
        putJson(j, "diff", diff);
        putJson(j, "compList", comps);
        return j;    
    } // end getCompsByDiff()
    
    @POST
    @Path("GetCompById")
    @Produces(MediaType.APPLICATION_JSON) 
    public JSONObject getCompById(@QueryParam("id") int id) {
        
        Comp c = cBean.findByIntX("Id", id);
        
        JSONObject j =  new JSONObject();
        putJson(j, "status", "gotCompById");
        putJson(j, "title", c.getTitle());
        putJson(j, "author", c.getAuthor());
        putJson(j, "length", c.getLength());
        putJson(j, "year", c.getYear());
        putJson(j, "diff", c.getDiff());
        putJson(j, "pages", c.getPages());
        putJson(j, "video", c.getVideo());
        putJson(j, "sheet", c.getSheet());
        putJson(j, "addTime", c.getAddtime());
        putJson(j, "adderId", c.getAdderid());
        putJson(j, "comms", c.getComms());
        return j;    
    } // end getCompById()
    
    // used for getting your own compositions
    @POST
    @Path("GetCompsByAdderId")
    @Produces(MediaType.APPLICATION_JSON) 
    public JSONObject getCompsByAdderId(@QueryParam("adderid") int adderId) {
        
        List<Comp> comps = cBean.findAllByIntX("AdderId", adderId);
        
        JSONObject j =  new JSONObject();
        putJson(j, "status", "gotCompsByAdderId");
        putJson(j, "adderId", adderId);
        putJson(j, "compList", comps);
        return j;     
    } // end getCompsById()
    
    // TODO: there is an issue with removal due to the Likes and Favorite tables referencing the Comp table; fix this asap!
    @POST
    @Path("RemoveComp")
    @Produces(MediaType.APPLICATION_JSON) 
    public JSONObject removeComp(@QueryParam("id") int id) { // the id comes from clicking on the composition to delete
        
        // TODO: check that you have the right to remove it (admin or adder)
        
        Comp c = cBean.findByIntX("Id", id);
        cBean.deleteFromDb(c);
        
        JSONObject j =  new JSONObject();
        putJson(j, "status", "removedComp");
        putJson(j, "title", c.getTitle());
        putJson(j, "author", c.getAuthor());
        putJson(j, "length", c.getLength());
        putJson(j, "year", c.getYear());
        putJson(j, "diff", c.getDiff());
        // not all stats are necessary to display after deletion
        return j;    
    } // end removeComp()
    
    // search through all compositions by their name (alphabetically)
    @POST
    @Path("TitleSearch")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject titleSearch(@FormParam("strToSearch") String str) {
        
        // this is a very crude and costly operation that quickly gets unworkable...
        // we can fine-tune it if we have the time
        
        if (isEmpty(str)) { // empty or contains pure whitespace
            
            return setResponseStatus("emptySearchString"); // unnecessary if we use js on client to prevent the sending of empty searches
        }
        
        List<Comp> comps = cBean.getAllComps(); // double overhead because we fetch all comps first w/out conditions... an actual db query with a stricter argument could be used instead
        ArrayList<Comp> resultComps = new ArrayList<>();
        int length = comps.size();
        
        for (int i = 0; i < length; i++) {
            
            if (comps.get(i).getTitle().startsWith(str)) {
                
                resultComps.add(comps.get(i));
            }       
        }
                
        JSONObject j = new JSONObject();
        putJson(j, "status", "searchCompleted");
        putJson(j, "compList", resultComps);
        return j;
    } // end titleSearch()

    // There are a million billion stats that it's possible to change... This method should take care of all of them
    // NOTE: this may not work as I thought... how will the statName argument be sent multiple times if you choose to edit 
    // multiple fields at once ??
    @POST
    @Path("EditComp")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject editComp(@QueryParam("id") int compId, @QueryParam("newStat") String statName, @QueryParam("newVal") String statValue) {
        
        // TODO: remove the overlap between utils.Validation.validComp() and this validation...
        
        int ownId = 1; // PLACEHOLDER! Needs to come from the request header!      
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
        
        JSONObject j = new JSONObject();
        putJson(j, "status", status);
        putJson(j, "newValue", statValue);         
        return j;
        // TODO: send a msg to the user that they've changed the stat
    } // end changeStat()
} // end class
