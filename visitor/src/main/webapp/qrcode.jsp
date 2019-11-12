<!DOCTYPE html>
<%@ page contentType="text/html; charset=gb2312"%>
<html lang="en">
<head>
    <title>�ÿͶ�ά��</title>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.0/css/bootstrap-combined.min.css">
    <link rel="stylesheet" href="https://cdn.bootcss.com/twitter-bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-toast-plugin/1.3.2/jquery.toast.min.css">
    <style>
        /*web background*/
    .container{
        display:table;
        height:100%;
    }
        .child {
            position: absolute;
            top: 20%;
            left: 20%;
        }

    </style>

</head>
<body>
<div class="container">
    <br/>
    <h3 style="text-align:center">�ÿͶ�ά�� </h3>
    <div class="child">
        <div id="qrcodeTable"></div>
        <img  style="height: 60vw; width:60vw"  id='imgOne' />

<%--            <img  style="height: 80vw; width:90vw"  id='imgOne' />--%>
<%--            <h5 id="introduction">�뽫����ά���׼����ʶ���豸 </h5>--%>
        </div>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
<script src="https://cdn.bootcss.com/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.1.2/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-toast-plugin/1.3.2/jquery.toast.min.js"></script>
<script language=javascript>
    $(function() {
        var qrtext=null;

        var id=getUrlParam('id');
        var data ={id:id};
        // ��ʼ������
        $.ajax({
            type: 'POST',
            contentType: "application/x-www-form-urlencoded",
            url: "qrcode/qrcode",
            data: data,
            success: function(data){
                var obj = JSON.parse(data);
                // var obj = JSON.parse(data);
                // console.info("data.verify.desc:"+obj.verify.desc);
               var utfCode= toUtf8(obj.verify.desc);
                console.log(utfCode);
                if (obj.verify.sign=="fail"){
                 $('#introduction').html("��ȡ��ά��ʧ�ܣ�");
                 console.log("��ȡ��ά��ʧ�ܣ�");
                }else {
                    var qrcode = jQuery('#qrcodeTable').qrcode({

                        render: "canvas",                <!--��ά�����ɷ�ʽ -->
                        text: utfCode, <!-- ��ά������  -->
                        width: "300",               //��ά��Ŀ��
                        height: "300",
                    }).hide();
                    var canvas = qrcode.find('canvas').get(0);
                    $('#imgOne').attr('src', canvas.toDataURL("image/png"));
                }
            }
        });
    });

    function getUrlParam(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //����һ������Ŀ�������������ʽ����
        var r = window.location.search.substr(1).match(reg);  //ƥ��Ŀ�����
        if (r != null) return unescape(r[2]); return null; //���ز���ֵ
    }
    function toUtf8(str) {
        var out, i, len, c;
        out = "";
        len = str.length;
        for(i = 0; i < len; i++) {
            c = str.charCodeAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out += str.charAt(i);
            } else if (c > 0x07FF) {
                out += String.fromCharCode(0xE0 | ((c >> 12) & 0x0F));
                out += String.fromCharCode(0x80 | ((c >>  6) & 0x3F));
                out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));
            } else {
                out += String.fromCharCode(0xC0 | ((c >>  6) & 0x1F));
                out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));
            }
        }
        return out;
    }

    // $("#btn1").click(function(){
    //     var test="haha";
    //     console.log("���ͬ��");
    //     $.ajax({
    //         type: 'POST',
    //         contentType: "application/x-www-form-urlencoded",
    //         url: "qrcode/qrcode",
    //         data: 1,
    //         success: function(data){
    //
    //         }
    //
    //     });
    //
    //     jQuery('#qrcodeTable').qrcode({
    //
    //         render    : "table",                <!--��ά�����ɷ�ʽ -->
    //         text    : test , <!-- ��ά������  -->
    //         width : "200",               //��ά��Ŀ��
    //         height : "200",
    //     });
    // });



</script>
</body>
</html>