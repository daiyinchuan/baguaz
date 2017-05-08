/**
 * Created by Administrator on 2016-7-29.
 */
(function () {

    // 获取 wangEditor 构造函数和 jquery
    var E = window.wangEditor;
    var $ = window.jQuery;

    // 用 createMenu 方法创建菜单
    E.createMenu(function (check) {

        // 定义菜单id，不要和其他菜单id重复。编辑器自带的所有菜单id，可通过『参数配置-自定义菜单』一节查看
        var menuId = 'quotepost';

        // check将检查菜单配置（『参数配置-自定义菜单』一节描述）中是否该菜单id，如果没有，则忽略下面的代码。
        if (!check(menuId)) {
            return;
        }

        // this 指向 editor 对象自身
        var editor = this;

        // 创建 menu 对象
        var menu = new E.Menu({
            editor: editor,  // 编辑器对象
            id: menuId,  // 菜单id
            title: '引用整个帖子', // 菜单标题

            // 正常状态和选中状态下的dom对象，样式需要自定义
            $domNormal: $('<a href="#" tabindex="-1"><i class="am-icon-comment-o"></i></a>'),
            $domSelected: $('<a href="#" tabindex="-1"><i class="am-icon-comment-o"></i></a>')
        });

        // 菜单正常状态下，点击将触发该事件
        menu.clickEvent = function (e) {
            // 使用自定义命令
            function commandFn() {
                var quoteAuthor,quoteContent;
                if($("#f_reply_topic input[name='replyto']").length>0){
                    var url=$("#f_reply_topic input[name='replyto']").next().attr("href")
                    var floor=url.split("?")[1]
                    var $floor=$("#"+floor.replace("=","loor-"))
                    quoteAuthor=$floor.find(".reply-author").html();
                    quoteContent=$floor.find(".reply-content").html()
                    editor.$txt.append('<blockquote><p>'+quoteAuthor+':</p>' + quoteContent + '</blockquote>');
                }else{
                    var $topic_content = $('.topic_content');
                    var $topic_author=$(".topic_author");
                    if ($topic_content.html()== "" && $topic_author.html()=="") {
                        $.getJSON("/community/getTopicQuote?topid=" + $.trim($('#topid').html()), function (ret) {
                            if(ret.map.quote.content.indexOf("<iframe")<0){
                                $topic_content.html(ret.map.quote.content)
                            }
                            $topic_author.html(ret.map.quote.authorname)
                            quoteAuthor=$topic_author.html()
                            quoteContent=$topic_content.html()
                            editor.$txt.append('<blockquote><p>'+quoteAuthor+':</p>' + quoteContent + '</blockquote>');
                        })
                    }else{
                        quoteAuthor=$topic_author.html()
                        quoteContent=$topic_content.html()
                        editor.$txt.append('<blockquote><p>'+quoteAuthor+':</p>' + quoteContent + '</blockquote>');
                    }
                }
            }
            editor.customCommand(e, commandFn);
        }

        // 增加到editor对象中
        editor.menus[menuId] = menu;
    });

})();