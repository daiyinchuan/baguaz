/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016-8-13 18:37:45                           */
/*==============================================================*/


drop table if exists bgz_attachment;

drop table if exists bgz_attachment_index;

drop table if exists bgz_category;

drop table if exists bgz_category_priv;

drop table if exists bgz_copyfrom;

drop table if exists bgz_download;

drop table if exists bgz_download_data;

drop table if exists bgz_flink;

drop table if exists bgz_hits;

drop table if exists bgz_member;

drop table if exists bgz_member_mpermission;

drop table if exists bgz_member_mrole;

drop table if exists bgz_message;

drop table if exists bgz_metatask;

drop table if exists bgz_model;

drop table if exists bgz_mpermission;

drop table if exists bgz_mrole;

drop table if exists bgz_msg_contact;

drop table if exists bgz_news;

drop table if exists bgz_news_data;

drop table if exists bgz_notification;

drop table if exists bgz_page;

drop table if exists bgz_permission;

drop table if exists bgz_picture;

drop table if exists bgz_picture_data;

drop table if exists bgz_position;

drop table if exists bgz_position_data;

drop table if exists bgz_rank;

drop table if exists bgz_reply;

drop table if exists bgz_role;

drop table if exists bgz_role_permission;

drop table if exists bgz_section;

drop table if exists bgz_session;

drop table if exists bgz_site;

drop table if exists bgz_task;

drop table if exists bgz_topic;

drop table if exists bgz_urlrule;

drop table if exists bgz_user;

drop table if exists bgz_user_role;

drop table if exists bgz_validcode;

drop table if exists bgz_video;

drop table if exists bgz_video_data;

/*==============================================================*/
/* Table: bgz_attachment                                        */
/*==============================================================*/
create table bgz_attachment
(
   id                   int(10) unsigned not null auto_increment,
   catid                smallint(5) unsigned not null default 0,
   filename             char(50) not null,
   filepath             char(200) not null,
   filesize             int(10) unsigned not null default 0,
   fileext              char(10) not null,
   isimage              tinyint(1) unsigned not null default 0 comment '0:不是,1:是',
   userid               mediumint(8)	 unsigned not null default 0,
   uploadtime           int(10) unsigned not null default 0,
   uploadip             char(15) not null,
   authcode             char(32) not null,
   primary key (id),
   key authcode (authcode)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_attachment_index                                  */
/*==============================================================*/
create table bgz_attachment_index
(
   keyid                char(30) not null,
   aid                  int(10) not null,
   key keyid (keyid),
   key aid (aid)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_category                                          */
/*==============================================================*/
create table bgz_category
(
   catid                smallint(5) unsigned not null auto_increment,
   type                 tinyint(2) not null comment '0:栏目,1:单页,2:外链',
   modelid              smallint(5)	 unsigned not null default 0,
   parentid             smallint(5) unsigned not null default 0,
   arrparentid          varchar(255) not null,
   child                tinyint(2) unsigned not null default 0 comment '0:没有,1:有;默认0',
   arrchildid           mediumtext,
   catname              varchar(30) not null,
   image                varchar(100) not null,
   description          mediumtext,
   parentdir            varchar(100),
   catdir               varchar(30),
   url                  varchar(100),
   items                mediumint(8) unsigned not null default 0,
   setting              mediumtext comment 'json格式数据',
   listorder            smallint(5) unsigned not null default 0,
   ismenu               tinyint(2) unsigned not null default 1 comment '0:不显示,1:显示;默认1',
   primary key (catid)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_category_priv                                     */
/*==============================================================*/
create table bgz_category_priv
(
   catid                smallint(5) unsigned not null default 0,
   roleid               smallint(5) unsigned not null default 0,
   key catid (catid, roleid)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_copyfrom                                          */
/*==============================================================*/
create table bgz_copyfrom
(
   id                   smallint(5) unsigned not null auto_increment,
   name                 varchar(30) not null,
   url                  varchar(100),
   thumb                varchar(100),
   listorder            smallint(5) unsigned default 0,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_download                                          */
/*==============================================================*/
create table bgz_download
(
   id                   mediumint(8) unsigned not null auto_increment,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_download_data                                     */
/*==============================================================*/
create table bgz_download_data
(
   id                   mediumint(8) unsigned not null default 0,
   key id (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_flink                                             */
/*==============================================================*/
create table bgz_flink
(
   id                   mediumint(8) unsigned not null auto_increment,
   name                 varchar(80) not null,
   icon                 varchar(100),
   url                  varchar(100),
   listorder            smallint(5) unsigned not null default 0,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_hits                                              */
/*==============================================================*/
create table bgz_hits
(
   hitsid               char(30) not null,
   catid                smallint(5) not null,
   views                int(10) not null default 0,
   yesterdayviews       int(10) not null default 0,
   dayviews             int(10) not null default 0,
   weekviews            int(10) not null default 0,
   monthviews           int(10) not null default 0,
   updatetime           int(10) unsigned not null default 0,
   primary key (hitsid)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_member                                            */
/*==============================================================*/
create table bgz_member
(
   id                   int unsigned not null auto_increment,
   password             varchar(64),
   nickname             varchar(128),
   lastloginip          varchar(15),
   lastlogintime        int(10) unsigned,
   email                varchar(40),
   avatar               varchar(255),
   url                  varchar(255),
   signature            varchar(1000),
   score                int(11) unsigned not null default 0,
   regtime              int(10) unsigned not null,
   token                varchar(16) comment '用户唯一标识，用于客户端和网页存入cookie',
   primary key (id)
)
engine = InnoDB
auto_increment = 10000;

/*==============================================================*/
/* Table: bgz_member_mpermission                                */
/*==============================================================*/
create table bgz_member_mpermission
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_member_mrole                                      */
/*==============================================================*/
create table bgz_member_mrole
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_message                                           */
/*==============================================================*/
create table bgz_message
(
   id                   int(11) unsigned not null auto_increment,
   mctid                int(11) unsigned not null,
   content              mediumtext not null,
   authorid             int unsigned not null,
   inputtime            int(10) unsigned not null,
   primary key (id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_metatask                                          */
/*==============================================================*/
create table bgz_metatask
(
   id                   mediumint(8) unsigned not null auto_increment,
   name                 varchar(45) not null,
   score                int(11) unsigned,
   kind                 tinyint(3) unsigned not null comment '1:日常任务（每日凌晨0点刷新）,
            2:新手任务（一次性的，10级以后清理，不刷新）',
   num                  tinyint(3) not null comment '0表示没有可用次数',
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_model                                             */
/*==============================================================*/
create table bgz_model
(
   id                   smallint(5) unsigned not null auto_increment,
   name                 char(30) not null,
   description          char(100),
   tablename            char(20) not null,
   default_style        char(30) not null,
   category_template    char(30) not null,
   list_template        char(30) not null,
   show_template        char(30) not null,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_mpermission                                       */
/*==============================================================*/
create table bgz_mpermission
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_mrole                                             */
/*==============================================================*/
create table bgz_mrole
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_msg_contact                                       */
/*==============================================================*/
create table bgz_msg_contact
(
   id                   int(11) unsigned not null auto_increment,
   authorid             int unsigned not null,
   to_authorid          int unsigned not null,
   msg_count            int(11) unsigned not null,
   last_msg_content     mediumtext not null,
   inputtime            int(10) unsigned not null,
   last_msg_time        int(10) unsigned not null,
   isdelete             tinyint(2) unsigned not null default 0 comment '1:删除,0:默认',
   primary key (id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_news                                              */
/*==============================================================*/
create table bgz_news
(
   id                   mediumint(8) unsigned not null auto_increment,
   catid                smallint(5) unsigned not null default 0,
   title                varchar(80) not null default '',
   thumb                varchar(100) not null default '',
   keywords             char(40) not null default '',
   description          mediumtext not null,
   posids               tinyint(2) unsigned not null default 0 comment '0:不在,1:在',
   url                  char(100),
   listorder            tinyint(3) unsigned not null default 0,
   sysadd               tinyint(2) unsigned not null default 0 comment '0:否,1:是',
   username             char(20) not null,
   inputtime            int(10) unsigned not null default 0,
   updatetime           int(10) unsigned not null default 0,
   primary key (id),
   key listorder (catid, listorder, id),
   key catid (catid, id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_news_data                                         */
/*==============================================================*/
create table bgz_news_data
(
   id                   mediumint(8) unsigned not null default 0,
   content              mediumtext,
   gimage               mediumtext,
   gfile                mediumtext,
   gaudio               mediumtext,
   gvideo               mediumtext,
   template             varchar(30),
   relation             varchar(255),
   copyfrom             varchar(100),
   key id (id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_notification                                      */
/*==============================================================*/
create table bgz_notification
(
   id                   int(11) unsigned not null auto_increment,
   content              varchar(255) not null,
   isread               tinyint(2) unsigned not null comment '1:已读,0:默认',
   from_authorid        int unsigned not null,
   authorid             int unsigned not null,
   targetid             varchar(255),
   inputtime            int(10) unsigned not null,
   action               varchar(255),
   source               tinyint(3) unsigned comment '1:topic,2:message',
   primary key (id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_page                                              */
/*==============================================================*/
create table bgz_page
(
   catid                smallint(5) unsigned not null default 0,
   title                varchar(160) not null,
   keywords             varchar(40) not null,
   content              text not null,
   updatetime           int(10) unsigned not null default 0 comment 'unix时间戳',
   key catid (catid)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_permission                                        */
/*==============================================================*/
create table bgz_permission
(
   id                   mediumint(6) not null auto_increment,
   name                 varchar(50) not null,
   value                varchar(50) not null,
   description          varchar(50),
   pid                  mediumint(6) not null,
   listorder            smallint(5),
   url                  varchar(255),
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_picture                                           */
/*==============================================================*/
create table bgz_picture
(
   id                   mediumint(8) unsigned not null auto_increment,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_picture_data                                      */
/*==============================================================*/
create table bgz_picture_data
(
   id                   mediumint(8) unsigned not null default 0,
   key id (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_position                                          */
/*==============================================================*/
create table bgz_position
(
   id                   smallint(5) unsigned not null auto_increment,
   modelid              smallint(5) unsigned default 0,
   catid                smallint(5) unsigned default 0,
   name                 char(30) not null,
   maxnum               smallint(5) unsigned not null default 20,
   listorder            smallint(5) unsigned not null default 0,
   thumb                varchar(150)	 not null,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_position_data                                     */
/*==============================================================*/
create table bgz_position_data
(
   id                   mediumint(8) unsigned not null default 0,
   catid                smallint(5) unsigned not null default 0,
   posid                smallint(5) unsigned not null default 0,
   modelid              smallint(6) unsigned default 0,
   thumb                tinyint(2) unsigned not null default 0,
   data                 mediumtext default NULL,
   listorder            mediumint(8) unsigned not null default 0,
   synedit              tinyint(2) unsigned not null default 0 comment '0:不同步,1:同步',
   key posid (posid),
   key listorder (listorder)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_rank                                              */
/*==============================================================*/
create table bgz_rank
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_reply                                             */
/*==============================================================*/
create table bgz_reply
(
   id                   int(11) unsigned not null auto_increment,
   topid                mediumint(8) unsigned not null,
   pid                  int(11) unsigned not null default 0,
   content              mediumtext not null,
   inputtime            int(10) unsigned not null,
   authorid             int unsigned not null,
   target_authorid      int unsigned not null default 0,
   floor                mediumint(8) unsigned not null default 0,
   best                 tinyint(2) unsigned not null default 0 comment '1:采纳,0:默认',
   isdelete             tinyint(2) unsigned not null default 0 comment '1:删除,0:默认',
   primary key (id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_role                                              */
/*==============================================================*/
create table bgz_role
(
   id                   tinyint(3) unsigned not null auto_increment,
   name                 varchar(50) not null,
   description          text,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_role_permission                                   */
/*==============================================================*/
create table bgz_role_permission
(
   roleid               tinyint(3) not null,
   permissionid         mediumint(6) not null
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_section                                           */
/*==============================================================*/
create table bgz_section
(
   id                   smallint(5) unsigned not null auto_increment,
   name                 varchar(45) not null,
   tab                  varchar(45) not null,
   parentid             smallint(5) unsigned not null,
   arrparentid          varchar(255) not null,
   child                tinyint(2) not null default 0 comment '0:有,1:没有;默认为0',
   arrchildid           mediumtext,
   url                  varchar(255),
   setting              mediumtext,
   listorder            smallint(5) unsigned not null default 0,
   isdisplay            tinyint(2) not null default 1 comment '0:不显示,1:显示;默认1',
   primary key (id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_session                                           */
/*==============================================================*/
create table bgz_session
(
   id                   char(32) not null,
   userid               mediumint(8) unsigned not null default 0,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_site                                              */
/*==============================================================*/
create table bgz_site
(
   siteid               smallint(5) unsigned not null auto_increment,
   name                 char(30) not null default '',
   domain               char(255),
   site_title           char(255),
   keywords             char(255),
   description          char(255),
   default_style        char(50) default NULL,
   template             text default NULL,
   setting              mediumtext default NULL comment 'json格式数据',
   uuid                 char(40) not null default '',
   primary key (siteid)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_task                                              */
/*==============================================================*/
create table bgz_task
(
   mid                  int unsigned not null,
   taskid               mediumint(8) unsigned not null,
   num                  tinyint(3) unsigned not null
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_topic                                             */
/*==============================================================*/
create table bgz_topic
(
   id                   mediumint(8) unsigned not null auto_increment,
   secid                smallint(5) unsigned not null,
   title                varchar(255) not null,
   content              mediumtext not null,
   inputtime            int(10) unsigned not null,
   updatetime           int(10) unsigned,
   authorid             int unsigned not null,
   view                 int(11) not null default 0,
   top                  tinyint(2) unsigned not null default 0 comment '1:置顶,0:默认',
   good                 tinyint(2) unsigned not null default 0 comment '1:精华,0:默认',
   last_reply_time      int(10) unsigned,
   last_reply_authorid  int unsigned,
   reply_count          int(11) unsigned not null default 0 comment '可增可减',
   floor_count          mediumint(8) unsigned not null default 0 comment ' 只增不减',
   url                  varchar(255),
   isdisplay            tinyint(2) unsigned not null default 1 comment '0:不显示,1:显示;默认1',
   isdelete             tinyint(2) unsigned not null default 0 comment '1:删除,0:默认',
   primary key (id)
)
engine = InnoDB;

/*==============================================================*/
/* Table: bgz_urlrule                                           */
/*==============================================================*/
create table bgz_urlrule
(
   id                   smallint(5) unsigned not null auto_increment,
   file                 varchar(20) not null,
   ishtml               tinyint(2) unsigned not null default 0 comment '0:不生成,1:生成',
   urlrule              varchar(255) not null,
   example              varchar(255) not null,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_user                                              */
/*==============================================================*/
create table bgz_user
(
   id                   mediumint(6) unsigned not null auto_increment,
   username             varchar(20) not null,
   password             varchar(64) not null,
   lastloginip          varchar(15),
   lastlogintime        int(10) unsigned default 0,
   email                varchar(40) not null,
   realname             varchar(50) default NULL,
   primary key (id),
   key username (username)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_user_role                                         */
/*==============================================================*/
create table bgz_user_role
(
   userid               mediumint(6) not null,
   roleid               tinyint(3) not null
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_validcode                                         */
/*==============================================================*/
create table bgz_validcode
(
   id                   int(11) unsigned not null auto_increment,
   code                 char(6) not null,
   gentime              int(10) unsigned not null,
   status               tinyint(2) not null comment '1:已使用,默认0',
   type                 tinyint(3) not null comment '1:会员用户注册,2:会员用户密码找回',
   expiretime           int(10) not null,
   target               varchar(255) not null,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_video                                             */
/*==============================================================*/
create table bgz_video
(
   id                   mediumint(8) unsigned not null auto_increment,
   primary key (id)
)
engine = MYISAM;

/*==============================================================*/
/* Table: bgz_video_data                                        */
/*==============================================================*/
create table bgz_video_data
(
   id                   mediumint(8) unsigned not null default 0,
   key id (id)
)
engine = MYISAM;

