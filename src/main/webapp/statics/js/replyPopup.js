/**
 * Created by Dai Yinchuan on 2016-7-31.
 */
(function(factory){

    factory(window.jQuery)

})(function($){

    // 验证是否引用jquery
    if (!$ || !$.fn || !$.fn.jquery) {
        alert('在引用replyPopup.js之前，先引用jQuery，否则无法使用 replyPopup');
        return;
    }

    // 定义扩展函数
    var _rp = function (fn) {
        var RP = window.replyPopup;
        if (RP) {
            // 执行传入的函数
            fn(RP, $);
        }
    };

    // 定义构造函数
    (function(window,$){
        if (window.replyPopup) {
            // 重复引用
            alert('一个页面不能重复引用 replyPopup.js 或 replyPopup.min.js ！！！');
            return;
        }

        // 回复弹框（整体）构造函数
        var RP = function (elem) {

            // ------------------初始化------------------
            this.init();
        }

        RP.fn = RP.prototype;

        // 暴露给全局对象
        window.replyPopup = RP;


    })(window,$);

    _rp(function(RP,$){
        RP.fn.init = function () {

            this.$replycontrol=$("#reply-control");

        };
    });

    _rp(function(RP,$){
        RP.fn.open=function(){

        }

        RP.fn.toggle=function(){

        }

        RP.fn.submit=function(){

        }

        RP.fn.cancel=function(){

        }

    });

    // 最终返回replyPopup构造函数
    return window.replyPopup;


/*    var _replypopup = function() {
        var id = 0;

        this.next = function() {
            return id++;
        };

        this.reset = function() {
            id = 0;
        }
    }

    window.replypopup={}
    _replypopup.apply(replypopup)

    return replypopup;*/
})