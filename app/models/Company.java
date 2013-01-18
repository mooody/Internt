/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import play.db.jpa.Model;
import models.Core.Module;
import play.data.validation.Required;

/**
 *
 * @author weetech
 */
@Entity
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
	public String street;
	public String zipcode;
	public String city;
	public String phone;
	public String cellphone;
	public String website;
	
	
 
    /**
     * Användare som är kopplade till företaget
     */
    @OneToMany(fetch=FetchType.LAZY)
    @JoinColumn(name="user_company", referencedColumnName="id")
	public List<UserBase> users;
	
	@ManyToMany
	 @JoinTable(name = "multiple_companies", 
        inverseJoinColumns = {@JoinColumn(name ="user_id") }, 
        joinColumns = { @JoinColumn(name = "company_id") })
    public List<UserBase> usersWithMultipleAccounts;
	
	@ManyToMany
	 @JoinTable(name = "company_modules", 
        inverseJoinColumns = {@JoinColumn(name ="module_id") }, 
        joinColumns = { @JoinColumn(name = "company_id") },
		uniqueConstraints = @UniqueConstraint(name = "oneOneModulePerCompany",
		columnNames = {"company_id", "module_id"})
	)
    public List<Module> modules;

    /**
     * 
     */
    @OneToOne
    public Contact contact;
    
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

}
