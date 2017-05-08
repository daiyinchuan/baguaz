import ch.qos.logback.classic.encoder.PatternLayoutEncoder

def USER_HOME = System.getProperty("user.home");
println "USER_HOME=${USER_HOME}"

scan("5 seconds")
appender("stdout", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
	pattern = "%d{HH:mm:ss.SSS} %-5level [%thread] %logger{16}:%L - %m%n"
  }
}
appender("FILE", RollingFileAppender) {
	file="baguaz.log"
/*	rollingPolicy(TimeBasedRollingPolicy) {
		fileNamePattern = "baguaz.%d{yyyy-MM-dd}.log"
		maxHistory = 10
	}*/
	encoder(PatternLayoutEncoder) { pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %C:%L - %m%n" }
}
logger("com.baguaz",DEBUG)
/*logger("com.baguaz.module.category.CategoryAdminController",ERROR)
logger("com.baguaz.module.index.IndexAdminController",ERROR)
logger("com.baguaz.module.category.Category",ERROR)
logger("com.baguaz.module.community.section.Section",ERROR)
logger("com.baguaz.pagetpl",DEBUG)
logger("org.apache.shiro",ERROR)
logger("com.jfinal.core.ActionHandler",WARN)
logger("com.jfinal",DEBUG)
logger("druid.sql.Statement",DEBUG)
logger("org.apache.shiro.web.session.mgt.DefaultWebSessionManager",ERROR)
*/
root(DEBUG, ["stdout"/*,"FILE"*/])