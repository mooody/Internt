/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import play.Logger;
import play.db.jpa.Model;

/**
 * Hanterar IP kontroll, kontrollerar om samma ipadress har försökt logga in 5 ggr på 30 min.
 * Då spärrar ipadressen i 30 min.
 * @author mikael
 */
@Entity(name="core.ip")
@Table(name="core_ip")
public class IP extends Model
{
    String ip;
    Integer count;
    @Temporal(TemporalType.TIMESTAMP)
    Date last;
    public IP(){}
    public IP(String _ip)
    {
        this.ip = _ip;
        this.count = 1;
    }
    private static int inc(String ip)
    {
        IP obj = IP.find("byIp", ip).first();
        if(obj!=null){
                obj.count ++;
        }
        else
        {
                obj = new IP(ip);			
        }
        obj.last = new Date();
        obj.save();	
        return obj.count;
    }
    public static boolean toManyTimes(String ip)
    {
        Calendar calendar = GregorianCalendar.getInstance();
        //check 30 seconds back
        calendar.add(GregorianCalendar.MINUTE, -30);
        Date date = calendar.getTime();
        //hämtar de object som har mer än 5 träffar samt varit aktiv inom 30 sekunder
        IP IpObject = IP.find("select ip from core.ip ip where ip.ip = :ip and ip.count > 5 and ip.last > :date")
                        .bind("ip",ip)
                        .bind("date", date)
                        .first();

        Logger.info("IpObject (%s=%s) %s", date, IpObject!=null?IpObject.last:0 ,IpObject!=null?IpObject.count:0);

        if(IpObject!=null)
        {
                return true;
        } 
        else 
        {
                IP.inc(ip);
        }
        //remove all old ones
        calendar.add(GregorianCalendar.MINUTE, -30);

        IP.delete("delete from core.ip i where i.last < ?", calendar.getTime());
        Logger.info("Deleted %s", IP.count());
        return false;
    }
	
}
