/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.Core;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import models.Company;
import play.Logger;
import play.db.jpa.Model;

/**
 * Class som håller i när modulerna går ut.
 * Du skapar en genom konstrultorn 
 * 
 * Ex ett företag betalar för 10 dagar framåt
 * 
 * Calendar calendar =GregorianCalendar.getInstance();
 * calendar.setTime(new Date());
 * calendar.roll(Calendar.DAY_OF_YEAR, 10)
 * ModuleExpire mex = new ModuleExpire(user().company, module, calendar.getTime());
 * int daysLeft = mex.daysToExpire(user().company, module)
 * @author weetech
 */
//@Entity
//@Table(name="core_moduleexpire"
//       uniqueConstraints = { 
//            @UniqueConstraint( columnNames = { "company_id", "module_id" } ) 
//          } 
//       )
//public class ModuleExpire extends Model
//{
//   @ManyToOne
//   Company company;
//   @ManyToOne
//   Module module;
//   @Temporal(TemporalType.DATE)
//   Date expire;
//   
//   public Date getExpire(){ return this.expire;}
//   public void setExpire(Date _date){this.expire = _date;}
//   public void update(Company company, Module module, Date expire)
//   {
//      ModuleExpire mEx = ModuleExpire.find("byCompanyAndModule", company, module).first();
//         mEx.setDate(expire);
//         mEx.save();
//   }
//   public ModuleExpire(Company _company, Module _module, Date _date)
//   {
//      this.company = _company;
//      this.expire = _date;
//      this.module = _module;
//      this.save();
//   }
//   
//   /**
//    * Calculates the days before the module expires. If not expire exists the method
//    * will return Integer.MaxValue;
//    * @param company
//    * @return 
//    */
//   public int daysToExpire(Company company, Module module)
//   {
//      ModuleExpire mEx = ModuleExpire.find("byCompanyAndModule", company, module).first();
//      
//      if(mEx == null) 
//      {
//         return Integer.MAX_VALUE;
//      }      
//      else
//      {         
//         long milliseconds = mEx.expire.getTime()-(new Date()).getTime();
//         long seconds = milliseconds / 1000;
//         long minutes = seconds / 60;
//         long hours = minutes / 60;
//         int days = (int)hours / 24;
//         return days;         
//      }
//   }
//}


