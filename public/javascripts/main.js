

jQuery(document).ready(function(){
	
	visualAtButtons();
	
	
});

function visualAtButtons()
{
	//byter klass på knappar
	jQuery("a.button").mouseover(function(){
		jQuery(this).addClass("overbutton");
	});
	jQuery("a.button").mouseout(function(){
		jQuery(this).removeClass("overbutton");
	});
	
	jQuery("input[type=submit]").mouseover(function(){
		jQuery(this).addClass("overbutton");
	});
	jQuery("input[type=submit]").mouseout(function(){
		jQuery(this).removeClass("overbutton");
	});
}