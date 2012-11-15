/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.ModuleController;
import controllers.PlanController;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import models.Core.Module;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MetaValue;
import play.Logger;
import play.data.validation.Required;
//import play.db.jpa.JPASupport;
import play.db.jpa.Model;
import utils.language.Language;


/**
 *
 * @author weetech
 */
@Entity
@Inheritance
@DiscriminatorColumn(name="UserType",discriminatorType = DiscriminatorType.STRING, length = 32)
@Table(name="User")
public class UserBase extends Model{
 
 
    @Required
    public String name;
    @Required
    public String password;

    @Column(unique = true)
    @Required
    public String email;
    
    public String lang;
    
    @Transient
    public Language language;
    
    @ManyToOne
    @JoinColumn(name="user_company")
    public Company company;
    
    
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(targetEntity=Content.class)
    @JoinTable(name = "content_read_by_user", 
        joinColumns = {@JoinColumn(name ="user_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "content_id") })
    public List<Content> contentsRead;
    
    public void setCompany(Company _company)
    {
        this.company = _company;
    }
    
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(mappedBy="users")
    public List<AccessRights> rights;
    
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(mappedBy="users")
    public List<Module> modules;
    
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(targetEntity=Grupp.class, mappedBy="users")
    public List<Grupp> groups;
    
    @Basic(fetch=FetchType.LAZY)
    @OneToMany(mappedBy="owner")
    public List<Content> contents;
   
    /**
     * Samma som this.groups
     * @return 
     */
    public List<Grupp> getGroups()
    {
       /* List<Grupp> groups = Grupp.find("select g from Grupp g left join UserBase u where u.id = :uid")
                .bind("uid", this.id)
                .fetch();*/
        return this.groups;
    
        
    }
    
    /**
     * Skapar en lista på alla grupper som användaren är med i. 
     * Den tar alla grupper som användaren har, samt alla barngrupper som
     * hör till dessa grupper.
     * @return 
     */
    public List<Grupp> getAllGroups()
    {
        try{
            List<Grupp> usergroups = new ArrayList();
            usergroups.addAll(this.groups);
            //Gå igenom alla grupper och hämta barnen
            for(Grupp g: this.groups)
            {
                Logger.info("UserBase.getAllGroups %s", g.name);
                if(g.childs!=null)
                {
                    for(Grupp c: g.childs)
                    {
                        Logger.info("%s", c.name);
                    }
                }

                usergroups.addAll(Grupp.findChilds(g));
            }

            return usergroups;
        
        } catch(Exception ex){
            Logger.warn("Exeption in UserBase.getAllGroups (user == null ??) %s", ex.getMessage());
            //throw ex;
        }
        return null;
    }
    
    public static UserBase login(String email, String pw)
    {
        UserBase user = UserBase.find("select u from UserBase u where u.email = :email and u.password = :pw").bind("email", email).bind("pw", pw).first();
        return user;
    }       
    
    /**
     * Kopierar alla fält från den medskickade parameterns user till den här.
     * 
     * @param user 
     */
    public void copyFields(UserBase user)
    {
        if(user.id>0)
        {
            this.id = user.id;
        }
        this.name = user.name;
        this.password = user.password;
        this.email = user.email;
    }
    
    /**
     * Returnerar true om användaren är av klassen Admin
     * @return 
     */
    public boolean isAdmin()
    {
        return this instanceof Admin;
        
    }
    
    /**
     * Språkfiler laddas in för varje gång som användaren anropas!
     * Den läser ut denna användarens lang-variabel och hämtar in klasssen till denna
     * 
     * Ska detta läggas i sessionvariabel istället?
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException 
     */
    @PostLoad
    public void loadLanguage() throws ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException
    {
        
        String name = this.lang;
       if(name==null)
           name = "Sv";
            Class cl = Class.forName("utils.language."+name);
            java.lang.reflect.Constructor co = cl.getConstructor();
            this.language =  (Language) co.newInstance();
           
            Logger.info("PostLoad UserBase");
    }
    
    
    @Override
    public String toString()
    {
        return this.name+" "+this.id;
    }
    
    
    
    //<editor-fold defaultstate="collapsed" desc=" Grupper">
    
    /**
     * Kontrollerar om användaren är i denna grupp.
     * Detta sker genom ett anrop till nedanstående isInGroupList
     * @param group
     * @return gruppens id nummer
     */
    public long isUserInGroup(Grupp group)
    {
        try{
        
            List<Grupp> usergroups = new ArrayList();
            UserBase u = UserBase.findById(this.id);
            usergroups.addAll(u.groups);
            return this.isGroupInList(usergroups, group);
            
        } catch(Exception ex) {
            Logger.info("Ex in User.isUserInGroup %s",ex.getMessage());
        }
        
        
        return 0;
    }
    
    
    /**
     * Metod som kontrollerar om gruppen finns i listan som skickas med.
     * 
     * @param grouplist Lista med grupper att kontrollera
     * @param group gruppen att kontrollera
     * @return true om gruppen hittades, annars false
     */
    public long isGroupInList(List<Grupp> grouplist, Grupp group )
    {
        Logger.info("UserBase.groupFound size:%d", grouplist.size());
        for(Grupp g : grouplist)
        {
            if(g.id == group.id)
            {
                Logger.info("UserBase.groupFound %s is in %s", this.name, group.name);
                return g.id;
            }
            else
            {
                long id = this.isGroupInList(g.childs, group);
                if(id > 0)
                {
                    return id;
                }
            }
        }
        return 0;
    }
      /**
     * Lägger till en användare i gruppen. 
     * @param groupId id of group
     * @param userId id of user
     * @return 0 = ok, 1 = user already in group, 2 = error
     */
    public long addGroupToUser(Long groupId) throws Exception
    {
        Grupp grupp = Grupp.findById(groupId);
        //User tempUser = User.findById(userId);
        long gId = this.isUserInGroup(grupp);
        if(gId>0)
        {
            Logger.info("UserBase.addGroupToUser - user in group");
            return gId;
        }
        
        try{
            grupp.users.add(this);        
            grupp.save();
        } catch (Exception ex){
            Logger.error("Catch in UserBase.addGroupToUser gid %s, uid%s", groupId, this.id);
            throw ex;
        }
            
        return 0;
        
    }
    
    /**
     * Funktionen tar bort användaren från gruppen
     * @param groupId (gruppens id som ska tas bort)
     * @return 0 om ok, 1 om användaren inte är i gruppen och 2 om ett fel inträffat
     */
    public int removeUserFromGroup(Long groupId)
    {
         Grupp grupp = Grupp.findById(groupId);
        
        if(this.isUserInGroup(grupp)>0)
        {
            return 1;
        }
        
        
        try{
            grupp.users.remove(this);
            grupp.save();
        
        } catch(Exception ex){
            Logger.error("Catch in UserBase.removeUserFromGroup gid %s, uid%s", groupId, this.id);
            return 2;
        }
            
        return 0;
    }
    //</editor-fold>
}
