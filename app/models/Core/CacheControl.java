package models.Core;
import javax.persistence.Entity;
import javax.persistence.Table;
import models.booking.BookingYear;
import play.Logger;
import play.db.jpa.Model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * CacheController fungerar så att du i modulen skapar en subclass med de ingående cacheparametrarna
 * Max 32 parametrar per class pga typen integer
 * 
 * Ex 
 * 
 * class ModuleCache extends CacheControl
 * {
 *    //Ex. parametrar
 *    public final static ONE = 1<<0;
 *    public final static TWO = 1<<1;
 *    public final static THREE = 1<<2;
 *    
 *    //Ex. om det krävs någon koppling i modulen.
 *    public ObjectInModule connectedTo;
 * }
 * 
 * Use
 * 
 * List<T> object = new ArrayList<T>();
 * ModuleCache cache = new ModuleCache()
 * cache.set(ModuleCache.ONE)
 * cache.setCached("mySessionKey", object);
 * 
 * if(cache.isCached(ModuleCache.one))
 * {
 *    Object = cache.getCached("mySessionKey", List.class);
 * }
 *
 * 
 * 
 * @author weetech
 */
//@Entity
//@Table(name="booking_cache_controll")
public abstract class CacheControl// extends Model
{
   //om 1 finns det cache:at om noll ska det hämtas
   public int cache;
   
   public boolean isCached(int cacheControl)
   {
      return (this.cache & cacheControl) > 0;
   }

   /**
    * Unsets the cacheControl param
    * @param cacheControl 
    */
   public void unset(int cacheControl)
   {       
      this.cache=(this.cache&(~cacheControl)); 
   }
   
   public <T> T getCacheParam(String key, Class<T> clazz)
   {      
      return play.cache.Cache.get(key, clazz);
   }
   
   public void setCacheParam(String key, Object object, int cacheControl)
   {
      this.cache |= cacheControl;
      play.cache.Cache.set(key, object, play.Play.configuration.getProperty("session.expire","8h"));
   }
   /**
    * Just debug, prints out the binary of num
    * @param num 
    */
   private void print(int num){
      for(int i=31; i>=0; i--)
      {
         if((num & (1<<i))>0){
            System.out.print("1");
         }
         else
         {
            System.out.print("0");
         }
      }
      System.out.println("");
   }
   
   
   
   
}
