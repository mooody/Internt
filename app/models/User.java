/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import play.db.jpa.JPABase;

/**
 *
 * @author weetech
 */
@Entity
@DiscriminatorValue("User")
public class User extends UserBase{
  
   
}
