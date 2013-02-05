// /*
//  * To change this template, choose Tools | Templates
//  * and open the template in the editor.
//  */
// package models;
// 
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;
// import javax.persistence.*;
// import play.db.jpa.GenericModel;
// import javax.persistence.GeneratedValue;
// import javax.persistence.Id;
// import javax.persistence.MappedSuperclass;
// import models.Core.Module;
// import play.Logger;
// import play.mvc.Controller;
// 
// /**
//  *
//  * @author weetech
//  */
// @Entity
// @Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
// public abstract class Content extends GenericModel{
//    
//     @Id
//     @GeneratedValue(strategy = GenerationType.TABLE)
//     public Long id;
// 
//     public Long getId() {
//         return id;
//     }
// 
//     @Override
//     public Object _key() {
//         return getId();
//     }
//     
//  //   @Basic(fetch=FetchType.LAZY)
//  //   @ManyToMany
//  //   @JoinTable(name = "content_read_by_user", 
//  //       joinColumns = {@JoinColumn(name ="content_id") }, 
//  //       inverseJoinColumns = { @JoinColumn(name = "user_id") })
//  //   public List<UserBase> hasRead;
//     
//     //flyktig variabel som visar på om ämnet är läst eller inte
//     @Transient
//     public boolean read = true;
//     
// //    @OneToOne
// //    public AccessRights rights;    
//     
//     @ManyToOne
//     public UserBase owner;
//    
//     //grupp id som innehållet tillhör
//     public long groupId;
//     
//     public Date date_created;
//     public Date date_edited;
//     public Date date_erased;
//     public boolean erased;
//     
//     public long parentId;
//     
//     /**
//      * If groupId == null ska innehållet vara globalt synbart inom företaget
//      * @return 
//      */
//     @Transient
//     public Grupp getGroup()
//     {
//         Grupp group = Grupp.findById(this.groupId);
//         return group;
//               
//     }
//     
//     public void setGroupIdentity(long id)
//     {
//         this.groupId = id;
//     }
//     
//     
//     public List<Content> readCompanyContent(UserBase user)
//     {
//         List<Content> contents = Content.find("select c from Content c "
//                 + "join left User u "
//                 + "where u.company == c.company").fetch();
//         return contents;
//     }
//     
//     public List<Content> readGroupContent(UserBase user) throws Exception
//     { 
//         throw new Exception("Content.readGroupContent - NOT DONE YEAT");
//         /*List<Grupp> groups = new ArrayList();
//         
//         for(Grupp g: user.groups)
//         {
//             groups.addAll(Grupp.findChilds(g));
//         }
//          List<Content> contents = Content.find("select c from Content c "
//                 + "join left User u "
//                 + "where u.company == c.company").fetch();
//        
//        
//         return contents;
//         */
//     
//     }
//     
//     public abstract boolean read(UserBase user);
//     public abstract boolean write(UserBase user);
//     public abstract boolean edit(UserBase user);
//     public abstract boolean erase(UserBase user);  
//     
//      @Override
//     public Content save()
//     {
//         Logger.info("content.save");
//         
//         /* lägg till ägare */
//         //TODO! Fixa avläsningen av session
//         //UserBase user = UserBase.findById(new Controller().session.get("userid"))
//         this.owner = (UserBase) play.cache.Cache.get("sessionuser", UserBase.class); 
//       
//         if(this.owner == null)
//         {
//             Logger.info("No SessionUser");
//             //flash.put("Message", "Content not saved! No sessionUser");
//         }
//         
//         //Lägg till skapande datum
//         if(this.id == null){
//             Logger.info("created");
//             this.date_created = new Date();
//         }
//         else{
//             Logger.info("Edited");
//             this.date_edited = new Date();
//         }
//         super.save();
//         
//         return this;      
//     }
//      @Override
//     public Content delete()
//     {
//         Logger.info("content.delete");
//         super.delete();
//         return this;      
//     }
//      
//      @Transient
//      public boolean readByUser(UserBase user)
//      {
//           if(this.hasRead.contains(user))
//           {
//               return true;
//           }
//           
//           return false;
//      }
// }
// 