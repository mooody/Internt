// /*
//  * To change this template, choose Tools | Templates
//  * and open the template in the editor.
//  */
// package models;
// 
// import java.util.List;
// import javax.persistence.*;
// import play.db.jpa.Model;
// 
// /**
//  *
//  * @author weetech
//  */
// @Entity
// public class AccessRights extends Model{
// 
//     public enum UserType{USER};
//     public enum Rights{
//         READ(1),
//         WRITE(2),
//         DELETE(4),
//         EDIT(8),
//         ADMIN(16);
//     
//         private int access = 0;  
// 
//         private Rights(int access) {  
//             this.access |= access;  
//         }  
// 
//         public int getAccess() {  
//             return access;  
//         }
//         
//         public boolean hasReadAccess()
//         {
//             if((this.access&0x01)!=0) 
//             {
//                 return true;
//             }
//             return false;
//                 
//         }
//     };
//     
//     
//      
//     //public Content content;
//     @ManyToMany
//     public List<Grupp> groups;
//     @ManyToMany
//     public List<UserBase> users;
//     
//     @Enumerated(EnumType.ORDINAL)
//     public Rights access;
//     
//     @OneToOne(mappedBy="rights")
//     public Content content;
// }
// // 