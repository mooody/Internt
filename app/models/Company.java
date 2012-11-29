/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import play.db.jpa.Model;

/**
 *
 * @author weetech
 */
@Entity
public class Company extends Model{
    
    /**
     * företagets namn
     */
    public String name;
    /**
     * Företagets orgnr
     */
    public String orgnr;
	public boolean fskatt;
	public double defaultDebitation = 0.0;
	public String bankgiro;
	public String plusgiro;
	public String mail;
	public String street;
	public String zipcode;
	public String city;
	public String phone;
	public String cellphone;
	public String defaultReferens = "";
	public int invoiceNumber = 0;
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

    /**
     * 
     */
    @OneToOne
    public Contact contact;
    
    /*@OneToMany(fetch=FetchType.LAZY, targetEntity=UserBase.class, mappedBy="company")
    @JoinColumn(name="user_company", referencedColumnName="id")
    public List<UserBase> getUsers(){
        return this.users;
    };*/
    
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
