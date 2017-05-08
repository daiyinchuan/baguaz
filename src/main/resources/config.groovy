import com.jfinal.kit.PathKit

[
	'devMode':'true',
	'adminSpeedLogin':'true',
	
	'db.type':'mysql',
	'db.url':'jdbc:mysql://localhost/baguaz?characterEncoding=utf8',
	'db.username':'root',
	'db.password':'111111',
	'db.prefix':'bgz',
	'db.showsql':'true',
	
	'email.smtp':'smtp.sina.com',
	'email.username':'baguaz168',
	'email.password':'o-h@t0Jqqvje0F',
	'email.mailfrom':'baguaz168@sina.com',
	'email.sendername':'baguaz',
	
	'tpl.root':'/WEB-INF/templates',
	
	'JS_PATH':'/statics/js',
	'CSS_PATH':'/statics/css',
	'IMG_PATH':'/statics/images',
	
	'UPLOAD_URL':"/uploadfile",
	'upload_path':PathKit.getWebRootPath()+File.separator+"uploadfile",
	
	'upload_maxsize':'2048000',
	
	'search_el_ip':'192.168.8.128',
	'search_el_port':'9300',
	
	'task_cron':"*/1 16 * * *"
]