
package controller;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import model.Comp;

/**
 * Session bean to manage Composition objects and their interaction with the database.
 * @author Ville L
 */
@Stateless
public class CompBean {

    @PersistenceContext
    private EntityManager em;
    
    public List<Comp> getAllComps() {

        return em.createNamedQuery("Comp.findAll").getResultList();
    }
        
    public Comp findByStrX(String stat, String arg) {
        
        try {
            Comp c = (Comp)em.createNamedQuery("User.findBy"+stat).setParameter(stat.toLowerCase(), arg).getSingleResult();         
            return c;
        } catch (NoResultException e) {
            return null;
        }    
    } // end findByStrX()
   
   // A 'findByX' method that works for ints instead of Strings (analogical to 'findById()' in UserBean.java, but here more int stats are needed)
   public Comp findByIntX(String stat, int arg) {
      
        try {
            Comp c = (Comp)em.createNamedQuery("Comp.findBy"+stat).setParameter(stat.toLowerCase(), arg).getSingleResult();         
            return c;
        } catch (NoResultException e) {
            return null;
        }  
   } // end findByIntX()
   
      // A method to fetch all compositions with a certain int stat
   public List<Comp> findAllByIntX(String stat, int arg) {
      
        try {
            List<Comp> c = (List<Comp>)em.createNamedQuery("Comp.findBy"+stat).setParameter(stat.toLowerCase(), arg).getResultList();
            return c;
        } catch (NoResultException e) {
            return null;
        }  
   } // end findAllByIntX()
    
    public Comp insertToDb(Comp c) {
       
        try { 
            em.persist(c);
        } 
        catch (Exception e) {
            e.printStackTrace();
            c = null;
        }
        return c;
    }
    
    public void updateDbEntry(Comp c){
        
        em.merge(c); // replace existing entry in the db with the updated one
    }
    
    // delete composition from the database (hopefully...)
    public void deleteFromDb(Comp c) {
        
        em.remove(em.merge(c));
        //em.createNamedQuery("Comp.deleteComp").setParameter("id", c.getId()).getSingleResult();
    }    
} // end class
