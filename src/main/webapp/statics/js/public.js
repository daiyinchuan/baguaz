/**
 * Created by daiyc on 2015-12-29.
 */
var bgz = {};

bgz.getChildFrame=function(index){
    return window.top.layer.getChildFrame('body', index);
}

bgz.iframeWin=function(layero){
    return window.top[layero.find('iframe')[0]['name']]
}


bgz.pagedata=function(page){
    page.data=page.list;
    page.list=null;
    return page.data;
}

bgz.openwinx=function(url,name,w,h) {
    if(!w) w=screen.width-4;
    if(!h) h=screen.height-45;
    var winx=window.open(url,name,"top=100,left=400,width=" + w + ",height=" + h + ",toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,status=no");
    var mtX=(window.outerWidth-winx.outerWidth)/2;
    var mtY=(window.outerHeight-winx.outerHeight)/2;
    winx.moveTo(mtX,mtY);
}


bgz.strlen_verify=function(obj, checklen, maxlen) {
    var v = obj.value, charlen = 0, maxlen = !maxlen ? 200 : maxlen, curlen = maxlen, len = strlen(v);
    for(var i = 0; i < v.length; i++) {
        if(v.charCodeAt(i) < 0 || v.charCodeAt(i) > 255) {
            curlen -= charset == 'utf-8' ? 2 : 1;
        }
    }
    if(curlen >= len) {
        $('#'+checklen).html(curlen - len);
    } else {
        obj.value = mb_cutstr(v, maxlen, true);
    }
}

bgz.strlen_cut=function(obj,val, checklen,maxlen) {
    var v = val, charlen = 0, maxlen = !maxlen ? 200 : maxlen, curlen = maxlen, len = strlen(v);
    for(var i = 0; i < v.length; i++) {
        if(v.charCodeAt(i) < 0 || v.charCodeAt(i) > 255) {
            curlen -= charset == 'utf-8' ? 2 : 1;
        }
    }
    if(curlen >= len) {
        $('#'+checklen).html(curlen - len);
    } else {
        obj.value = mb_cutstr(v, maxlen, true);
    }
}

var charset="utf-8";
var userAgent = navigator.userAgent.toLowerCase();
jQuery.browser = {
    version: (userAgent.match( /.+(?:rv|it|ra|ie)[\/: ]([\d.]+)/ ) || [0,'0'])[1],
    safari: /webkit/.test( userAgent ),
    opera: /opera/.test( userAgent ),
    msie: /msie/.test( userAgent ) && !/opera/.test( userAgent ),
    mozilla: /mozilla/.test( userAgent ) && !/(compatible|webkit)/.test( userAgent )
};
var strlen=function (str) {
    return ($.browser.msie && str.indexOf('\n') != -1) ? str.replace(/\r?\n/g, '_').length : str.length;
}
var mb_cutstr=function(str, maxlen, dot) {
    var len = 0;
    var ret = '';
    var dot = !dot ? '...' : '';
    maxlen = maxlen - dot.length;
    for(var i = 0; i < str.length; i++) {
        len += str.charCodeAt(i) < 0 || str.charCodeAt(i) > 255 ? (charset == 'utf-8' ? 3 : 2) : 1;
        if(len > maxlen) {
            ret += dot;
            break;
        }
        ret += str.substr(i, 1);
    }
    return ret;
}

bgz.ifrmAutoHeight=function(ifrm){
    var subWeb = document.frames ? document.frames[ifrm.id].document:ifrm.contentDocument;
    if(ifrm != null && subWeb != null) {
        ifrm.height = subWeb.body.scrollHeight;
    }
}

bgz.dteohover=function($dt){
    var hoverClass="dt-hover";
    $dt.delegate("tr","mouseover mouseout",function(event){
        if(event.type=="mouseover"){
            if($(this).hasClass("even")) {
                $(this).addClass(hoverClass)
            }else if($(this).hasClass("odd")){
                $(this).children().addClass(hoverClass)
            }
        }else if(event.type=="mouseout"){
            if($(this).hasClass("even")) {
                $(this).removeClass(hoverClass)
            }else if($(this).hasClass("odd")){
                $(this).children().removeClass(hoverClass)
            }
        }
    });
}