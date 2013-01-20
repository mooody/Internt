package controllers.mail;

import play.mvc.Controller;
import models.UserBase;
import play.i18n.Messages;

public class MailController extends Controller{

	public static void recoveryView()
	{
		render("Mails/recoveryView.html");
	}
	
	public static void sendRecovory(String email)
	{
		UserBase user = UserBase.find("byEmail", email).first();
		if(user == null)
		{
			validation.addError("email.not.found", "validation.email.not.found");
			render("Mails/recoveryView.html");
		}
		notifiers.Mails.lostPassword(user);
		flash("message", Messages.get("email.sent"));
		render("Mails/recoveryView.html");
	}
	public static void sendWelcome(String email)
	{
	
	}
}
    