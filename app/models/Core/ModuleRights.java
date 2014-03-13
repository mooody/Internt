package models.Core;

import java.util.SortedMap;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import models.Company;
import models.UserBase;
import play.Logger;


/**
 * Klass som håller i rättigheter mellan användare och modul.
 * 
 * Eg. booking ex. Initilized class with only one permission. (of maximum 32, (32 bit in db))
   public class Rights extends ModuleRights
   {

      public static final int ACCOUNTS_ALL = 1;

      public Rights()
      {
         listOfRights = new TreeMap<String, Integer>();
         //translate: 'booking.rights.accounts.all' in conf/message
         listOfRights.put("booking.rights.accounts.all", ACCOUNTS_ALL);

      }
   }
 * 
 * ex use in controller (booking.HeadBookingController)
 * Rights rights = Cache.get(session.getId() + "booking_rights", Rights.class);
   if(rights == null)
   {
      rights = Rights.find("byUser", user()).first();
      Cache.set(session.getId() + "booking_rights", rights,Play.configuration.getProperty("session.expire"));
   }

   renderArgs.put("premissions", rights);
   * 
   * When to check the permissions in views eg
   * #{if permissions == null || permissions?.hasRight(permissions?.ACCOUNTS_ALL)}
   * 
   * And in controllers the same... Default is permissions null = all permissions
 */
@Entity
@Table(name="core_modulerights")
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class ModuleRights extends play.db.jpa.GenericModel
{
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public Long id;
    
    /**
     * Används för att i modulen fylla på med vilka rättigheter man ska ha
     */
    @Transient
    public SortedMap<String,Integer> listOfRights;
    @Column(nullable=false)
    protected int rights = 0;
    @ManyToOne    
    public UserBase user;
    @ManyToOne    
    public Module module;
    @ManyToOne    
    public Company company;

    public ModuleRights() {
        this.listOfRights = null;
    }
    
    /**
     * Hämtar ut vektorn med rättigheter
     * @return 
     */
    public int getRights()
    {
        return this.rights;
    }
    
    public SortedMap<String,Integer> getListOfRights()
    {
        return listOfRights;
    }
    
    /**
     * Byter alla rättigheter mot parametern _rights
     * @param _rights 
     */
    public void setRights(int _rights)
    {
        this.rights = _rights;
    }
    
    /**
     * Lägger till rättigheten på den biten. (Ett stället bit)
     * @param bit 
     */
    public void addRight(int bit)
    {
        int mask = (0x01<<bit-1);
        this.rights = this.rights|mask;
    }
    
    /**
     * Tar bort rättigheten (nollställer biten)
     * @param bit 
     */
    public void removeRight(int bit)
    {
        int mask = ~(0x01<<bit-1);
        this.rights = this.rights&mask;
    }
    
    /**
     * Kontrollerar om rättigheten finns (kollar om biten är satt)
     * @param bit
     * @return 
     */
    public boolean hasRight(int bit)
    {
        int value = this.rights&(0x01<<(bit-1));
        Logger.info("%d %d %d %d",this.rights, (0x01<<(bit-1)) ,value, bit);
        return value>0?true:false;
    }
    
    public boolean hasRight(Integer bit)
    {
        return hasRight(bit.intValue());
    }
    
    /**
     * Skriver ut rättighetsvektorn. För debugsyfte
     */
    public void printRights()
    {
        int mask = 0x01;
        
        System.out.print("RIGHTS:");
        for(int i=0; i<8; i++)
        { 
            if(this.hasRight(mask))
            {
                System.out.print("1");
            }
            else{
                System.out.print("0");
            }
            
            mask++;
        }
        
        System.out.println();
    }
}