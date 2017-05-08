# 概述
- 这是一款java作为后端语言开发的cms（内容管理系统）。
- 基本功能：栏目管理，内容管理，模板风格，伪静态，静态，用户角色权限控制，推荐位，相关文章，点击量统计，模板标签，标签缓存，常用数据缓存，页面缩略图，全文搜索（elasticsearch+ik）等。
- 基于java8开发，简洁紧凑的Stream函数式语法。
- 基本组件：jfinal作为后端框架，beetl作为前端模板，shiro作为权限控制组件。

# 1 服务器端部署
1. 使用git clone代码，gradle编译打包成war包，或直接[下载war包](http://www.baidu.com)
2. 将`doc/create.sql`、`doc/init.sql`脚本在mysql数据库里运行，创建baguaz数据库
3. [下载jetty](http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.3.8.v20160314/jetty-distribution-9.3.8.v20160314.tar.gz)，另外jdk版本需要jdk8+
4. 将war包放到`jetty/webapps`，解压并使网站根目录为`jetty/webapps/root`
5. 修改配置文件`/WEB-INF/classes/config.groovy`的数据库配置信息
6. 启动jetty，在jetty根目录执行`bin/jetty.sh -d start`
7. 浏览器访问`http://部署的服务器ip:8080/`
8. 后台访问路径`http://部署的服务器ip:8080/admin`，admin账号/密码：`admin/admin1`

# 2 本地开发

## 2.1 部署
1. 使用git clone代码
2. gradle编译
3. 运行com.baguaz.cms.AppConfig类里面的main方法
4. 浏览器访问`http://localhost:8080/`

## 2.2 模板开发
1. 默认模板路径`/src/main/webapp/WEB-INF/templates/default`
2. 自定义模板路径`/src/main/webapp/WEB-INF/templates/自定义模板名称`

# 3 应用案例
- 给之前公司开发的公司产品官网[http://www.jimidun.com](http://www.jimidun.com)
- 自己的网站[http://www.baguaz.com](http://www.baguaz.com)

# 4 附录

## 4.1 nginx伪静态规则
	rewrite ^(.*)/show-([0-9]+)-([0-9]+)-([0-9]+)\.html$ $1/content/show?catid=$2&id=$3&page=$4 last;
	rewrite ^(.*)/content-([0-9]+)-([0-9]+)-([0-9]+)\.html$ $1/content/show?catid=$2&id=$3&page=$4 last;
	rewrite ^(.*)/list-([0-9]+)-([0-9]+)\.html$ $1/content/lists?catid=$2&page=$3 last;