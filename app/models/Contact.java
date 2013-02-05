// /*
//  * To change this template, choose Tools | Templates
//  * and open the template in the editor.
//  */
// package models;
// 
// import javax.persistence.Entity;
// import javax.persistence.OneToMany;
// import javax.persistence.OneToOne;
// import play.db.jpa.Model;
// 
// /**
//  *
//  * @author weetech
//  */
// @Entity
// public class Contact extends Model{
//     public String name;
//     public String cellphone;
//     public String phone;
//     public String address;
//     public String zipcode;
//     public String city;
//     public String customerId;
//     public String referens;
//     
//     @OneToOne(mappedBy="contact")
//     public Company company;
//     
//     /*@OneToMany(mappedBy="contact")
//     public Group group;
//     */
//     
//     /*@OneToMany(mappedBy="contact")
//     public Group subgroup;*/
//     
// }
// 