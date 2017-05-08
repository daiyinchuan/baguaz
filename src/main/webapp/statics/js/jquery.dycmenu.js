//闭包限定命名空间
(function ($) {
    $.fn.extend({
        "dycmenu": function (options) {
            //检测用户传进来的参数是否合法
            if (!isValid(options)){
            	return this;
            }
            var opts = $.extend({}, defaluts, options); 
            //使用jQuery.extend 覆盖插件默认参数
            return this.each(function () {  
            	//这里的this 就是 jQuery对象。这里return 为了支持链式调用
            	//遍历所有的要高亮的dom,当调用 highLight()插件的是一个集合的时候。
                var $this = $(this); 
                //获取当前dom 的 jQuery对象，这里的this是当前循环的dom//根据参数来设置 dom的样式
                /*$this.css({
                    backgroundColor: opts.background,
                    color: opts.foreground
                });*/

                var _ul=$this.find("li>ul>li>ul");
                $this.find("span").append('<i class="am-icon-chevron-down" style="float: right;"></i>')
                $this.find("span").on("click", function () {
                    if($(this).parent().find("ul").hasClass("unfold")){
                        $(this).parent().find("ul").removeClass("unfold");
                        $(this).find("i").removeClass("am-icon-chevron-up").addClass("am-icon-chevron-down");
                    }else{
                        var uf=_ul.filter(".unfold");
                        uf.removeClass("unfold");
                        uf.parent().find("i").removeClass("am-icon-chevron-up").addClass("am-icon-chevron-down");
                        $(this).parent().find("ul").addClass("unfold")
                        $(this).find("i").removeClass("am-icon-chevron-down").addClass("am-icon-chevron-up");
                    }
                })
            });
        }
    });
    //默认参数
    var defaluts = {
        //foreground: 'red',
        //background: 'yellow'
    };
    //公共的格式化 方法. 默认是加粗，用户可以通过覆盖该方法达到不同的格式化效果。
    /**
     * 展开菜单
     * @param str
     * @returns {string}

    $.fn.dycmenu.unfold = function (str) {
        //return "<strong>" + str + "</strong>";
    }*/
    /**
     * 折叠菜单
     * @param str

    $.fn.dycmenu.fold=function(str){

    }*/
    //私有方法，检测参数是否合法
    function isValid(options) {
        return !options || (options && typeof options === "object") ? true : false;
    }
})(window.jQuery);