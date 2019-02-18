////////////////////////////////////////////键盘事件////////////////////////////////

// 按Enter键发送信息
$(document).keydown(function(event){
    if(event.keyCode == 13){
        SendMsg();
    }
});






/////////////////////////////////////////////前台信息处理/////////////////////////////////////////////////////////
// 发送信息
function SendMsg()
{
    var text = document.getElementById("text");
    if (text.value == "" || text.value == null)
    {
        alert("发送信息为空，请输入！")
    }
    else
    {
    	//发送的时候改变UI
        AddMsg('default', SendMsgDispose(text.value));
        var retMsg = AjaxSendMsg(text.value)
        AddMsg('AI_PigPig', retMsg);
        text.value = "";
    }
}
// 发送的信息处理
function SendMsgDispose(detail)
{
    detail = detail.replace("\n", "<br>").replace(" ", "&nbsp;")
    return detail;
}

// 增加信息
function AddMsg(user,content)
{
    var str = CreadMsg(user, content);
    var msgs = document.getElementById("msgs");
    msgs.innerHTML = msgs.innerHTML + str;
}

// 生成内容
function CreadMsg(user, content)
{
    var str = "";
    if(user == 'default')
    {
        str = "<div class=\"msg guest\"><div class=\"msg-right\"><div class=\"msg-host headDefault\"></div><div class=\"msg-ball\" title=\"今天 17:52:06\">" + content +"</div></div></div>"
    }
    else
    {
        str = "<div class=\"msg robot\"><div class=\"msg-left\" worker=\"" + user + "\"><div class=\"msg-host photo\" style=\"background-image: url(../Images/head.png)\"></div><div class=\"msg-ball\" title=\"今天 17:52:06\">" + content + "</div></div></div>";
    }
    return str;
}



/////////////////////////////////////////////////////////////////////// 后台信息处理 /////////////////////////////////////////////////////////////////////////////////

// 发送
function AjaxSendMsg(_content)
{
//  var retStr = "";
//  $.ajax({
//      type: "POST",
//      async:false,
//      url: "/Home/ChatMethod/",
//      data: {
//          content: _content
//      },
//      error: function (request) {
//          retStr = "你好";
//      },
//      success: function (data) {
//          retStr = data.info;
//      }
//  });
//  return retStr;
			var retStr = "";
            window.CHAT = {
                socket: null,
                init: function() {
                    if (window.WebSocket) {
                        CHAT.socket = new WebSocket("ws://192.168.0.105:8088/ws");
                        CHAT.socket.onopen = function() {
                            console.log("连接建立成功...");
                        },
                        CHAT.socket.onclose = function() {
                            console.log("连接关闭...");
                        },
                        CHAT.socket.onerror = function() {
                            console.log("发生错误...");
                        },
                        CHAT.socket.onmessage = function(e) {
                        	retStr=e.data;
                            console.log("接受到消息：" + e.data);
                            //var receiveMsg = document.getElementById("receiveMsg");
                            //var html = receiveMsg.innerHTML;
                            //receiveMsg.innerHTML = html + "<br/>" + e.data;
                        }
                    } else {
                        alert("浏览器不支持websocket协议...");
                    }
                },
                chat: function() {
                    //var msg = document.getElementById("msgContent");
                    CHAT.socket.send(_content);
                }
            };
            
            CHAT.init();  
            return retStr;
}