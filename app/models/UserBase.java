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
import play.db.jpa.Model;
import utils.language.Language;
import play.*;
import utils.Cryptography;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;


/**
 *
 * @author weetech
 */
@Entity
@Inheritance
@DiscriminatorColumn(name="UserType",discriminatorType = DiscriminatorType.STRING, length = 32)
@Table(name="User")
@SqlResultSetMappings({
    @SqlResultSetMapping(name = "dummy", columns = {
        @ColumnResult(name = "nothing")})
})
@NamedNativeQuery(name="UserBase.changeUserType", 
    query="update User set UserType = :type where id = :id",
	resultSetMapping = "dummy")
public class UserBase extends Model{
 
	private static final String DES_ENCRYPTION_KEY = Play.configuration.getProperty("application.secret");
	
    @Required
    public String name;
    @Required
    private String password;
	public String getPassword() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		return Cryptography.decrypt(this.password,DES_ENCRYPTION_KEY);
	}
	
	public void setPassword(String _pw) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		this.password =Cryptography.encrypt(_pw,DES_ENCRYPTION_KEY);
	}

    @Column(unique = true)
    @Required
    public String email;
    
	public String street;
	public String zipcode;
	public String city;
	public String cellphone;
	public String phone;
	
    //public String lang;
    
    @Transient
    public Language language;
    
    @ManyToOne
    @JoinColumn(name="user_company")
    public Company company;
    
	@ManyToMany(cascade=CascadeType.ALL)
	 @JoinTable(name = "multiple_companies", 
        joinColumns = {@JoinColumn(name ="user_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "company_id") })
	public List<Company> companies;
    
	/*
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(targetEntity=Content.class)
    @JoinTable(name = "content_read_by_user", 
        joinColumns = {@JoinColumn(name ="user_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "content_id") })
    public List<Content> contentsRead;
    */
    public void setCompany(Company _company)
    {
        this.company = _company;
    }
    /*
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(mappedBy="users")
    public List<AccessRights> rights;
    */
    @Basic(fetch=FetchType.LAZY)
    @OneToMany
    public List<Module> modules;
    
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(targetEntity=Grupp.class, mappedBy="users")
    private List<Grupp> groups;
    
   /* @Basic(fetch=FetchType.LAZY)
    @OneToMany(mappedBy="owner")
    public List<Content> contents;*/
   
    /**
     * Samma som this.groups
     * @return 
     */
    public List<Grupp> getGroups()
    {
        return Grupp.getUsersGroupsInCompany(this);
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
            usergroups.addAll(this.getGroups());
            //Gå igenom alla grupper och hämta barnen
            for(Grupp g: this.getGroups())
            {
                usergroups.addAll(Grupp.findChilds(g));
            }

            return usergroups;
        
        } catch(Exception ex){
            Logger.warn("Exeption in UserBase.getAllGroups (user == null ??) %s", ex.getMessage());
            //throw ex;
        }
        return null;
    }
	
	public static String getCryptedPassword(String _pw) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		return Cryptography.encrypt(_pw,DES_ENCRYPTION_KEY);
	}
    
    public static UserBase login(String email, String pw) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
    {
        UserBase user = UserBase.find("select u from UserBase u where u.email = :email and u.password = :pw").bind("email", email).bind("pw", UserBase.getCryptedPassword(pw)).first();
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
		this.company = user.company;
		this.companies = user.companies;
		this.modules = user.modules;
		this.groups = user.groups;
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
     * Returnerar true om användaren är av klassen SuperAdmin
     * @return 
     */
    public boolean isSuperAdmin()
    {
        return this instanceof SuperAdmin;
    }
	
	/**
     * Returnerar true om användaren är av klassen Admin
     * @return 
     */
    public boolean isUser()
    {
        return this instanceof User;
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
    public void alert() 
    {
            Logger.info("PostLoad UserBase");
    }
    
    @Override
    public String toString()
    {
        return this.name+" "+this.id;
    }
    
    
	//Skapa initialer av användaren
	public String getSignature()
	{
		String[] initials_arr = this.name.split(" ");
		StringBuilder sb = new StringBuilder();
		for(String s: initials_arr)
		{
			sb.append(s.charAt(0));
		}
		
		return sb.toString();
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
