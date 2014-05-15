/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;
import models.Core.CompanyUserSettings;
import models.Core.Module;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 *
 * @author weetech
 */
@Entity
@Table(name = "core_company")
public class Company extends Model{

	
    /**
     * företagets namn
     */
    @Required(message="you.need.a.company.name")
    public String name;
    /**
     * Företagets orgnr
     */
    @Required(message="you.need.a.company.orgnr")
    public String orgnr;
    public String mail;
     @Required(message="you.need.a.company.street")
    public String street;
      @Required(message="you.need.a.company.zipcode")
    public String zipcode;
       @Required(message="you.need.a.company.city")
    public String city;
    public String phone;
    public String cellphone;
    public String website;

    @Column(name="users_count", nullable=false)
    private long usersCount=5;
    @Column(name="admin_count", nullable=false)
    private long adminsCount=1;
    @Column(name="privuser_count", nullable=false)
    private long privilegeUsersCount=1;
	
	
    public void setUsersCount(long _value){this.usersCount = _value;}
    public void setAdminsCount(long _value){this.adminsCount = _value;}
    public void setPrivilegeUsersCount(long _value){this.privilegeUsersCount = _value;}

    public long getUsersCount(){return this.usersCount;}
    public long getAdminsCount(){return this.adminsCount;}
    public long getPrivilegeUsersCount(){return this.privilegeUsersCount;}

    /**
     * Användare som är kopplade till företaget
     */
    @OneToMany(fetch=FetchType.LAZY)
    @JoinColumn(name="user_company", referencedColumnName="id")
    public List<UserBase> users;
	
    public List<UserBase> getUsers(){
        List<UserBase> temp =  users;
        if(this.usersWithMultipleAccounts == null) this.usersWithMultipleAccounts = new ArrayList<UserBase>();
        for(UserBase user:this.usersWithMultipleAccounts)
        {
            if(!temp.contains(user))
            {
                temp.add(user);
            }
        }
        return temp;
    }
    @ManyToMany
    @JoinTable(name = "core_multiple_companies", 
    inverseJoinColumns = {@JoinColumn(name ="user_id") }, 
    joinColumns = { @JoinColumn(name = "company_id") })
    public List<UserBase> usersWithMultipleAccounts;
	
    @ManyToMany
     @JoinTable(name = "core_company_modules", 
    inverseJoinColumns = {@JoinColumn(name ="module_id") }, 
    joinColumns = { @JoinColumn(name = "company_id") },
            uniqueConstraints = @UniqueConstraint(name = "oneOneModulePerCompany",
            columnNames = {"company_id", "module_id"})
    )
    public List<Module> modules;

	public List<Module> getCompanyModules()
	{
		return this.modules;
	}
    /**
     * 
     */
//    @OneToOne
//    public Contact contact;
    
    public List<Grupp> getCompanyGroups()
	{
		List<Grupp> groups = Grupp.find("select g from Grupp g where g.companyId = :cid")
			.bind("cid", this.id)
			.fetch();
		return groups;
	}
    
    /**
     * Lägger till användare till företaget
     * @param user 
     */
    public void addUser(UserBase user)
    {
        if(this.users == null) this.users = new ArrayList<UserBase>();
        this.users.add(user);
    }
	
    @Override
    public String toString()
    {
            return "Company:"+this.name;
    }

    public int countUsers()
    {
            //long count = UserBase.count("select count(u.id) from UserBase u where u.company.id = ?", this.id );
            return this.usersWithMultipleAccounts.size();
    }

    public static List<PrivilegeUser> getPrivilegeUsers(Company company)
    {
            return PrivilegeUser.find("byCompany", company).fetch();
    }
    
    public void createDefaultGroup(Admin admin){
    
        Grupp newGroup = new Grupp(this.id);
        newGroup.name = this.name;
        newGroup.users = new ArrayList();
        newGroup.users.add(admin);
        newGroup.save();
    }
    
    public CompanyUserSettings attachUserToCompanyToGetCUS(UserBase user){
        
        //addUserToCompany
        this.addUser(user);
        user.addCompany(this);
        CompanyUserSettings cus;
        try {
            cus = new CompanyUserSettings(user, this);
        } catch (CompanyUserSettings.CUSException ex) {
            cus=ex.getCus();
        }

        return cus;
        //CreateCUS
    }

}
