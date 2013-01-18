/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.List;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.Model;


/**
 *
 * @author weetech
 */
@Entity
public class Grupp extends Model{
    public String name;
    
    @ManyToOne(targetEntity=Grupp.class)
    public Grupp parent;
    
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany()
    public List<UserBase> users;
    
    @Basic(fetch=FetchType.LAZY)
    @OneToMany
    public List<Grupp> childs;
    
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(targetEntity=AccessRights.class,mappedBy="groups")
    public List<AccessRights> rights;
    
    private long companyId;
            
    
    public void setCompanyId(long _companyId)
    {
        this.companyId = _companyId;
    }
    
    public Grupp(long _companyId)
    {
        this.companyId = _companyId;
    }
    public long getCompanyId()
    {
        return this.companyId;
    }
  
    public static List<Grupp> findParents()
    {
        return Grupp.find("select g from Grupp g where g.parent = null").fetch();
    }
    
    public static List<Grupp>findChildsOneLevel(Grupp parent)
    {
            return Grupp.find("select g from Grupp g where g.parent.id = :crit").bind("crit", parent.id).fetch();
    }
    
    /**
     * Metod som kontrollerar om gruppen finns i listan som skickas med.
     * 
     * @param grouplist Lista med grupper att kontrollera
     * @param group gruppen att kontrollera
     * @return true om gruppen hittades, annars false
     */
    public static long isGroupInList(List<Grupp> grouplist, Grupp group )
    {
        
        Logger.info("Grupp.isGroupInList size:%d", grouplist.size());
        for(Grupp g : grouplist)
        {
            if(g.id == group.id)
            {
                Logger.info("Grupp.isGroupInList : in %s", group.name);
                return g.id;
            }
            else
            {
                long id = Grupp.isGroupInList(g.childs, group);
                if(id > 0)
                {
                    return id;
                }
            }
        }
        return 0;
    }
	
	public static List<Grupp> getUsersGroupsInCompany(UserBase user){
		List<Grupp> groups = Grupp.find("select g from Grupp g left join g.users u where g.companyId = :cid and u.id = :uid")
			.bind("cid", user.company.id)
			.bind("uid",user.id)
			.fetch();
			
			Logger.info("%s %s", user.name, user.company.name);
			for(Grupp grupp : groups)
			{
				Logger.info("%s %s", grupp.name, user.company.id);
			}
		return groups;
	}
    
    
    /**
     * Hämtar alla nivåer av barngrupper från parent och nedåt
     * @param parent
     * @return 
     */
    public static List<Grupp> findChilds(Grupp parent)
    {
        List<Grupp> groups = Grupp.find("select g from Grupp g where g.parent.id = :crit and g.companyId = :cid")
                .bind("crit", parent.id)
                .bind("cid",parent.companyId)
                .fetch();
        
        try{
            for(Grupp g: groups)//parent.childs)
            {
                groups.addAll(findChilds(g));
            }
        } catch(Exception ex)
        {
            Logger.error("Grupp.findChilds %s", ex.toString());
        }
        return groups;
    }
    
    @Override
    public String toString()
    {
        return this.name+" "+this.id;
    }
    
}
