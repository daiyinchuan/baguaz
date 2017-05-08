-- ----------------------------
-- Records of bgz_user
-- ----------------------------
INSERT INTO `bgz_user` VALUES ('1', 'admin', '686dd4419d5da11febc928e2f4df2daa16dd8e3d6b8201b80abf99a32f4d4b83', '127.0.0.1', '1452240593', 'daiyinchuan@163.com', '');
INSERT INTO `bgz_user` VALUES ('2', 'daiyc', 'abf23e46a68c3c22fbda96d4e24737915be6f9b2b2cf84a61fd71beac7d90565', null, '1458283599', 'daiyinchuan@163.com', '戴银川');
INSERT INTO `bgz_user` VALUES ('3', 'test', 'c566736ea650ed31379393746a73d67e903d014dd90d999709dd07641bd68682', null, '1458283546', 'daiyinchuan@163.com', '');

-- ----------------------------
-- Records of bgz_role
-- ----------------------------
INSERT INTO `bgz_role` VALUES ('1', '超级管理员', '超级管理员');
INSERT INTO `bgz_role` VALUES ('2', '站点管理员', '站点管理员');
INSERT INTO `bgz_role` VALUES ('3', '运营总监', '运营总监');
INSERT INTO `bgz_role` VALUES ('4', '总编', '总编');
INSERT INTO `bgz_role` VALUES ('5', '编辑', '编辑');
INSERT INTO `bgz_role` VALUES ('7', '发布人员', '发布人员');

-- ----------------------------
-- Records of bgz_permission
-- ----------------------------
INSERT INTO `bgz_permission` VALUES ('1', '我的面板', 'menu:mypanel', '菜单一级结点，我的面板', '0', '1', null);
INSERT INTO `bgz_permission` VALUES ('2', '设置', 'menu:set', '菜单一级结点，设置', '0', '2', null);
INSERT INTO `bgz_permission` VALUES ('3', '内容', 'menu:mcontent', '菜单一级结点，内容', '0', '3', null);
INSERT INTO `bgz_permission` VALUES ('4', '个人信息', 'menu:mypanel:personinfo', '菜单二级结点，我的面板-个人信息', '1', '1', null);
INSERT INTO `bgz_permission` VALUES ('5', '相关设置', 'menu:set:relatedset', '菜单二级结点，设置-相关设置', '2', '1', null);
INSERT INTO `bgz_permission` VALUES ('6', '用户设置', 'menu:set:userset', '菜单二级结点，设置-用户设置', '2', '2', null);
INSERT INTO `bgz_permission` VALUES ('7', '内容发布管理', 'menu:mcontent:cradmin', '菜单二级结点，内容-内容发布管理', '3', '1', null);
INSERT INTO `bgz_permission` VALUES ('8', '发布管理', 'menu:mcontent:radmin', '菜单二级结点，内容-发布管理', '3', '2', null);
INSERT INTO `bgz_permission` VALUES ('9', '内容相关设置', 'menu:mcontent:crelatedset', '菜单二级结点，内容-内容相关设置', '3', '3', null);
INSERT INTO `bgz_permission` VALUES ('10', '修改个人信息', 'user:editInfo', '功能结点，修改用户个人信息', '4', '1', '/admin/user/editInfo');
INSERT INTO `bgz_permission` VALUES ('12', '站点管理', 'menu:site', '既是菜单结点也是功能结点，站点管理初始查询', '5', '1', '/admin/site/init');
INSERT INTO `bgz_permission` VALUES ('13', '站点编辑', 'site:edit', '功能结点，站点编辑', '12', '1', '/admin/site/edit');
INSERT INTO `bgz_permission` VALUES ('14', '基本设置', 'menu:setting', '既是菜单结点也是功能结点，设置初始查询', '5', '2', '/admin/setting/init');
INSERT INTO `bgz_permission` VALUES ('15', '用户管理', 'menu:user', '既是菜单结点也是功能结点，用户管理初始查询', '6', '1', '/admin/user/init');
INSERT INTO `bgz_permission` VALUES ('16', '用户添加', 'user:add', '功能结点', '15', '1', '/admin/user/add');
INSERT INTO `bgz_permission` VALUES ('17', '用户编辑', 'user:edit', '功能结点', '15', '2', '/admin/user/edit');
INSERT INTO `bgz_permission` VALUES ('18', '用户删除', 'user:del', '功能结点', '15', '3', '/admin/user/del');
INSERT INTO `bgz_permission` VALUES ('19', '角色管理', 'menu:role', '既是菜单结点也是功能结点，角色管理初始查询', '6', '2', '/admin/role/init');
INSERT INTO `bgz_permission` VALUES ('20', '角色添加', 'role:add', '功能结点', '19', '1', '/admin/role/add');
INSERT INTO `bgz_permission` VALUES ('21', '角色编辑', 'role:edit', '功能结点', '19', '2', '/admin/role/edit');
INSERT INTO `bgz_permission` VALUES ('22', '角色删除', 'role:del', '功能结点', '19', '3', '/admin/role/del');
INSERT INTO `bgz_permission` VALUES ('23', '管理内容', 'menu:content', '既是菜单结点也是功能结点，管理内容初始查询', '7', '1', '/admin/content/init');
INSERT INTO `bgz_permission` VALUES ('24', '附件管理', 'menu:attachment', '既是菜单结点也是功能结点，附件管理初始查询', '7', '2', '/admin/attachment/init');
INSERT INTO `bgz_permission` VALUES ('25', '内容添加', 'content:add', '功能结点', '23', '1', '/admin/content/add');
INSERT INTO `bgz_permission` VALUES ('26', '内容编辑', 'content:edit', '功能结点', '23', '2', '/admin/content/edit');
INSERT INTO `bgz_permission` VALUES ('27', '内容删除', 'content:del', '功能结点', '23', '3', '/admin/content/del');
INSERT INTO `bgz_permission` VALUES ('28', '内容排序', 'content:listorder', '功能结点', '23', '4', '/admin/content/listorder');
INSERT INTO `bgz_permission` VALUES ('29', '生成首页', 'createhtml:genIndex', '功能结点', '8', '1', '/admin/createhtml/genIndex');
INSERT INTO `bgz_permission` VALUES ('30', '批量更新栏目页', 'createhtml:genCategory', '功能结点', '8', '2', '/admin/createhtml/genCategory');
INSERT INTO `bgz_permission` VALUES ('31', '批量更新内容页', 'createhtml:genShow', '功能结点', '8', '3', '/admin/createhtml/genShow');
INSERT INTO `bgz_permission` VALUES ('32', '批量更新URL', 'createhtml:updateUrls', '功能结点', '8', '4', '/admin/createhtml/updateUrls');
INSERT INTO `bgz_permission` VALUES ('33', '清理缓存', 'createhtml:clearCache', '功能结点', '8', '5', '/admin/createhtml/clearCache');
INSERT INTO `bgz_permission` VALUES ('34', '管理栏目', 'menu:category', '既是菜单结点也是功能结点，管理栏目初始查询', '9', '1', '/admin/category/init');
INSERT INTO `bgz_permission` VALUES ('35', 'URL规则管理', 'menu:urlrule', '既是菜单结点也是功能结点，URL规则管理初始查询', '9', '2', '/admin/urlrule/init');
INSERT INTO `bgz_permission` VALUES ('36', '来源管理', 'menu:copyfrom', '既是菜单结点也是功能结点，来源管理初始查询', '9', '3', '/admin/copyfrom/init');
INSERT INTO `bgz_permission` VALUES ('37', '推荐位管理', 'menu:position', '既是菜单结点也是功能结点，推荐位管理初始查询', '9', '4', '/admin/position/init');
INSERT INTO `bgz_permission` VALUES ('38', '栏目添加', 'category:add', '功能结点', '34', '1', '/admin/category/add');
INSERT INTO `bgz_permission` VALUES ('39', '栏目编辑', 'category:edit', '功能结点', '34', '2', '/admin/category/edit');
INSERT INTO `bgz_permission` VALUES ('40', '栏目删除', 'category:del', '功能结点', '34', '3', '/admin/category/del');
INSERT INTO `bgz_permission` VALUES ('41', '栏目排序', 'category:listorder', '功能结点', '34', '4', '/admin/category/listorder');
INSERT INTO `bgz_permission` VALUES ('42', '更新缓存', 'category:updateCache', '功能结点', '34', '5', '/admin/category/updateCache');
INSERT INTO `bgz_permission` VALUES ('43', '修改密码', 'user:editPwd', '功能结点', '4', '2', '/admin/user/editPwd');
INSERT INTO `bgz_permission` VALUES ('44', '模块', 'menu:module', '菜单一级结点，模块', '0', '4', null);
INSERT INTO `bgz_permission` VALUES ('45', '模块管理', 'menu:module:madmin', '菜单二级结点，模块-模块管理', '44', '1', null);
INSERT INTO `bgz_permission` VALUES ('46', '全站搜索', 'menu:search', '既是菜单结点也是功能结点，全站搜索初始查询', '45', '1', '/admin/search/init');
INSERT INTO `bgz_permission` VALUES ('47', '重建索引', 'search:createindex', '功能结点', '46', '1', '/admin/search/createindex');
INSERT INTO `bgz_permission` VALUES ('48', '删除索引', 'search:deleteindex', '功能结点', '46', '1', '/admin/search/deleteindex');
INSERT INTO `bgz_permission` VALUES ('49', '友情链接', 'menu:flink', '既是菜单结点也是功能结点，友情链接初始查询', '45', '1', '/admin/flink/init');
INSERT INTO `bgz_permission` VALUES ('50', '友情链接添加', 'flink:add', '功能结点', '49', '1', '/admin/flink/add');
INSERT INTO `bgz_permission` VALUES ('51', '友情链接编辑', 'flink:edit', '功能结点', '49', '2', '/admin/flink/edit');
INSERT INTO `bgz_permission` VALUES ('52', '友情链接删除', 'flink:del', '功能结点', '49', '3', '/admin/flink/del');
INSERT INTO `bgz_permission` VALUES ('53', '友情链接排序', 'flink:listorder', '功能结点', '49', '4', '/admin/flink/listorder');
INSERT INTO `bgz_permission` VALUES ('54', '栏目权限', 'role:catpriv', '功能结点', '19', '4', '/admin/role/catpriv');
INSERT INTO `bgz_permission` VALUES ('55', '用户', 'menu:mmember', '菜单一级结点，用户', '0', '5', null);
INSERT INTO `bgz_permission` VALUES ('56', '会员', 'menu:mmember:member', '菜单二级结点，用户-会员', '55', '1', null);
INSERT INTO `bgz_permission` VALUES ('57', '会员管理', 'menu:member', '既是菜单结点也是功能结点，会员管理的初始查询', '56', '1', '/admin/member/init');
INSERT INTO `bgz_permission` VALUES ('58', '社区', 'menu:mcommunity', '菜单一级结点，社区', '0', '6', null);
INSERT INTO `bgz_permission` VALUES ('59', '板块', 'menu:mcommunity:section', '菜单二级结点，社区-板块', '58', '1', null);
INSERT INTO `bgz_permission` VALUES ('60', '话题', 'menu:mcommunity:topic', '菜单二级结点，社区-话题', '58', '2', null);
INSERT INTO `bgz_permission` VALUES ('61', '回复', 'menu:mcommunity:reply', '菜单二级结点，社区-回复', '58', '3', null);
INSERT INTO `bgz_permission` VALUES ('62', '板块管理', 'menu:section', '既是菜单结点也是功能结点，板块管理的初始查询', '59', '1', '/admin/community/section/init');
INSERT INTO `bgz_permission` VALUES ('63', '话题管理', 'menu:topic', '既是菜单结点也是功能结点，话题管理的初始查询', '60', '1', '/admin/community/topic/init');
INSERT INTO `bgz_permission` VALUES ('64', '回复管理', 'menu:reply', '既是菜单结点也是功能结点，回复管理的初始查询', '61', '1', '/admin/community/reply/init');
INSERT INTO `bgz_permission` VALUES ('65', '板块添加', 'section:add', '功能结点', '62', '1', '/admin/community/section/add');
INSERT INTO `bgz_permission` VALUES ('66', '板块编辑', 'section:edit', '功能结点', '62', '2', '/admin/community/section/edit');
INSERT INTO `bgz_permission` VALUES ('67', '板块删除', 'section:del', '功能结点', '62', '3', '/admin/community/section/del');
INSERT INTO `bgz_permission` VALUES ('68', '板块排序', 'section:listorder', '功能结点', '62', '4', '/admin/community/section/listorder');
INSERT INTO `bgz_permission` VALUES ('69', '话题删除', 'topic:del', '功能结点', '63', '1', '/admin/community/topic/del');
INSERT INTO `bgz_permission` VALUES ('70', '回复删除', 'reply:del', '功能结点', '64', '1', '/admin/community/reply/del');
-- ----------------------------
-- Records of bgz_user_role
-- ----------------------------
INSERT INTO `bgz_user_role` VALUES ('1', '1');
INSERT INTO `bgz_user_role` VALUES ('2', '2');

-- ----------------------------
-- Records of bgz_role_permission
-- ----------------------------
INSERT INTO `bgz_role_permission` VALUES ('2', '34');
INSERT INTO `bgz_role_permission` VALUES ('2', '23');
INSERT INTO `bgz_role_permission` VALUES ('1', '1');
INSERT INTO `bgz_role_permission` VALUES ('1', '2');
INSERT INTO `bgz_role_permission` VALUES ('1', '3');
INSERT INTO `bgz_role_permission` VALUES ('1', '4');
INSERT INTO `bgz_role_permission` VALUES ('1', '5');
INSERT INTO `bgz_role_permission` VALUES ('1', '6');
INSERT INTO `bgz_role_permission` VALUES ('1', '7');
INSERT INTO `bgz_role_permission` VALUES ('1', '8');
INSERT INTO `bgz_role_permission` VALUES ('1', '9');
INSERT INTO `bgz_role_permission` VALUES ('1', '10');
INSERT INTO `bgz_role_permission` VALUES ('1', '12');
INSERT INTO `bgz_role_permission` VALUES ('1', '13');
INSERT INTO `bgz_role_permission` VALUES ('1', '14');
INSERT INTO `bgz_role_permission` VALUES ('1', '15');
INSERT INTO `bgz_role_permission` VALUES ('1', '16');
INSERT INTO `bgz_role_permission` VALUES ('1', '17');
INSERT INTO `bgz_role_permission` VALUES ('1', '18');
INSERT INTO `bgz_role_permission` VALUES ('1', '19');
INSERT INTO `bgz_role_permission` VALUES ('1', '20');
INSERT INTO `bgz_role_permission` VALUES ('1', '21');
INSERT INTO `bgz_role_permission` VALUES ('1', '22');
INSERT INTO `bgz_role_permission` VALUES ('1', '23');
INSERT INTO `bgz_role_permission` VALUES ('1', '24');
INSERT INTO `bgz_role_permission` VALUES ('1', '25');
INSERT INTO `bgz_role_permission` VALUES ('1', '26');
INSERT INTO `bgz_role_permission` VALUES ('1', '27');
INSERT INTO `bgz_role_permission` VALUES ('1', '28');
INSERT INTO `bgz_role_permission` VALUES ('1', '29');
INSERT INTO `bgz_role_permission` VALUES ('1', '30');
INSERT INTO `bgz_role_permission` VALUES ('1', '31');
INSERT INTO `bgz_role_permission` VALUES ('1', '32');
INSERT INTO `bgz_role_permission` VALUES ('1', '33');
INSERT INTO `bgz_role_permission` VALUES ('1', '34');
INSERT INTO `bgz_role_permission` VALUES ('1', '35');
INSERT INTO `bgz_role_permission` VALUES ('1', '36');
INSERT INTO `bgz_role_permission` VALUES ('1', '37');
INSERT INTO `bgz_role_permission` VALUES ('1', '38');
INSERT INTO `bgz_role_permission` VALUES ('1', '39');
INSERT INTO `bgz_role_permission` VALUES ('1', '40');
INSERT INTO `bgz_role_permission` VALUES ('1', '41');
INSERT INTO `bgz_role_permission` VALUES ('1', '42');
INSERT INTO `bgz_role_permission` VALUES ('1', '43');


-- ----------------------------
-- Records of bgz_copyfrom
-- ----------------------------
INSERT INTO `bgz_copyfrom` VALUES ('1', '腾讯网', 'www.qq.com', null, '1');
INSERT INTO `bgz_copyfrom` VALUES ('2', '新浪网', 'www.sina.com.cn', null, '2');
INSERT INTO `bgz_copyfrom` VALUES ('3', '新华网', 'www.xinhuanet.cn', null, '3');


-- ----------------------------
-- Records of bgz_model
-- ----------------------------
INSERT INTO `bgz_model` VALUES ('1', '文章模型', '', 'news', 'default', 'category', 'list', 'show');
INSERT INTO `bgz_model` VALUES ('2', '下载模型', '', 'download', 'default', 'category_download', 'list_download', 'show_download');
INSERT INTO `bgz_model` VALUES ('3', '图片模型', '', 'picture', 'default', 'category_picture', 'list_picture', 'show_picture');
INSERT INTO `bgz_model` VALUES ('11', '视频模型', '', 'video', 'default', 'category_video', 'list_video', 'show_video');


-- ----------------------------
-- Records of bgz_position
-- ----------------------------
INSERT INTO `bgz_position` VALUES ('9', '0', '0', '网站顶部推荐', '20', '0', '');
INSERT INTO `bgz_position` VALUES ('1', '0', '0', '首页焦点图推荐', '20', '1', '');
INSERT INTO `bgz_position` VALUES ('2', '0', '0', '首页头条推荐', '20', '4', '');
INSERT INTO `bgz_position` VALUES ('12', '0', '0', '首页图片推荐', '20', '0', '');
INSERT INTO `bgz_position` VALUES ('13', '82', '0', '栏目页焦点图', '20', '0', '');
INSERT INTO `bgz_position` VALUES ('10', '0', '0', '栏目首页推荐', '20', '0', '');
INSERT INTO `bgz_position` VALUES ('8', '30', '54', '图片频道首页焦点图', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('5', '69', '0', '推荐下载', '20', '0', '1', '');
--INSERT INTO `bgz_position` VALUES ('14', '0', '0', '视频首页焦点图', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('15', '0', '0', '视频首页头条推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('16', '0', '0', '视频首页每日热点', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('17', '0', '0', '视频栏目精彩推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('18', '0', '0', '视频首页焦点图推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('19', '0', '0', '视频首页头条推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('20', '0', '0', '视频首页每日热点推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('21', '0', '0', '视频栏目精彩推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('22', '0', '0', '视频首页焦点图推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('23', '0', '0', '视频首页头条推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('24', '0', '0', '视频首页每日热点推荐', '20', '0', '');
--INSERT INTO `bgz_position` VALUES ('25', '0', '0', '视频栏目精彩推荐', '20', '0', '');


-- ----------------------------
-- Records of bgz_site
-- ----------------------------
INSERT INTO `bgz_site` VALUES ('1', '默认站点', 'http://local.baguaz.com/', 'baguaz cms site title', 'baguaz cms site keywords', 'baguaz cms site description3', 'default', 'default', '{\"index_ishtml\":1}', '');


-- ----------------------------
-- Records of bgz_urlrule
-- ----------------------------
INSERT INTO `bgz_urlrule` VALUES ('1', 'category', '1', '${categorydir}${catdir}/index.html|${categorydir}${catdir}/${page}.html', 'news/china/1000.html');
INSERT INTO `bgz_urlrule` VALUES ('6', 'category', '0', 'content/lists?catid=${catid}|content/lists?catid=${catid}&page=${page}', '/content/lists?catid=1&page=1');
INSERT INTO `bgz_urlrule` VALUES ('11', 'show', '1', '${year}/${catdir}_${month}${day}/${id}.html|${year}/${catdir}_${month}${day}/${id}_${page}.html', '2010/catdir_0720/1_2.html');
INSERT INTO `bgz_urlrule` VALUES ('12', 'show', '1', '${categorydir}${catdir}/${year}/${month}${day}/${id}.html|${categorydir}${catdir}/${year}/${month}${day}/${id}_${page}.html', 'it/product/2010/0720/1_2.html');
INSERT INTO `bgz_urlrule` VALUES ('16', 'show', '0', 'content/show?catid=${catid}&id=${id}|content/show?catid=${catid}&id=${id}&page=${page}', '/content/show?catid=1&id=1');
INSERT INTO `bgz_urlrule` VALUES ('17', 'show', '0', 'show-${catid}-${id}-${page}.html', 'show-1-2-1.html');
INSERT INTO `bgz_urlrule` VALUES ('18', 'show', '0', 'content-${catid}-${id}-${page}.html', 'content-1-2-1.html');
INSERT INTO `bgz_urlrule` VALUES ('30', 'category', '0', 'list-${catid}-${page}.html', 'list-1-1.html');
INSERT INTO `bgz_urlrule` VALUES ('32', 'category', '1', '${topcatdir}/list/${uuid}.html|${topcatdir}/list/${uuid}_${page}.html', 'help/list/574c52191a3d9d6c8d9dc1ee_2.html');
INSERT INTO `bgz_urlrule` VALUES ('33', 'show', '1', '${topcatdir}/show/${uuid}.html', 'help/show/574c52191a3d9d6c8d9dc1ee.html');
INSERT INTO `bgz_urlrule` VALUES ('63', 'section', '0', 'community/s-${secid}.html|community/s-${secid}-l-${label}.html|community/s-${secid}-l-${label}-p-${page}.html', 'community/s-189-l-latest-p-1.html');
INSERT INTO `bgz_urlrule` VALUES ('64', 'section', '0', 'community/s-${tab}.html|community/s-${tab}-l-${label}.html|community/s-${tab}-l-${label}-p-${page}.html', 'community/s-plugin-l-top-p-32.html');
INSERT INTO `bgz_urlrule` VALUES ('65', 'thread', '0', 'community/t-${id}.html|community/t-${id}-p-${page}.html', 'community/t-12321-p-1.html');


-- ----------------------------
-- Records of bgz_metatask
-- ----------------------------
INSERT INTO `bgz_metatask` VALUES ('1', '每天第一次使用会员用户ID登录', '5', '1', '1');
INSERT INTO `bgz_metatask` VALUES ('2', '每天发布话题一次', '10', '1', '1');
INSERT INTO `bgz_metatask` VALUES ('3', '每天回复帖子三次', '3', '1', '3');