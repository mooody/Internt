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

	public enum CompanyType {AB, E, HB, KB, EK,KHF, BRF, BF, SF, I, S, FL, BAB, MB, SB, BFL, FAB, OFB, SE, SCE, TSF, X}
    /*
	AB = Aktiebolag.
	E = Enskild näringsidkare.
	HB = Handelsbolag.
	KB = Kommanditbolag.
	EK = Ekonomisk förening.
	KHF = Kooperativ hyresrättsförening.
	BRF = Bostadsrättsförening.
	BF = Bostadsförening.
	SF = Sambruksförening.
	I = Ideell förening som bedriver näring.
	S = Stiftelse som bedriver näring.
	FL = Filial till utländskt bolag.
	BAB = Bankaktiebolag.
	MB = Medlemsbank.
	SB = Sparbank.
	BFL = Utländsk banks filial.
	FAB = Försäkringsaktiebolag.
	OFB = Ömsesidigt försäkringsbolag.
	SE = Europabolag.
	SCE = Europakooperativ.
	TSF = Trossamfund.
	X = Annan företagsform.
	*/
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
	public CompanyType companyType = CompanyType.AB;
	
 
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
