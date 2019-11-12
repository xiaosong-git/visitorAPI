
<!DOCTYPE html>
<%@ page contentType="text/html; charset=gb2312"%>
<html lang="en">
<head>
    <title>同意邀约</title>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

    <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.0/css/bootstrap-combined.min.css">
    <link rel="stylesheet" href="https://cdn.bootcss.com/twitter-bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-toast-plugin/1.3.2/jquery.toast.min.css">
</head>
<body>
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <div class="row-fluid">
                <div class="span4">

                </div>
                <div class="span4">
                    <h3>
                       这是一个邀约界面
                    </h3>
                    <p>
                        <em id="invite">邀约</em>
                    </p>
<!--                    <form class="form-inline " method="post" action="#" style="margin-top: 20px" >-->
                    <div class="row-fluid">
                        <div class="span4">
                            <button class="btn btn-success btn-large" id="btn1" type="button">同意邀约</button>
                        </div>
                        <div class="span4">
                            <button class="btn btn-large btn-warning" id="btn2" type="button">拒绝邀约</button>
                        </div>
                        <div class="span4">

                        </div>
                    </div>
<!--                    </form>-->
                </div>
                <div class="span4">

                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
<script src="https://cdn.bootcss.com/twitter-bootstrap/4.3.1/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-toast-plugin/1.3.2/jquery.toast.min.js"></script>
<script language=javascript>

    $("#btn1").click(function(){

        agree("1");
    });
    $("#btn2").click(function(){
        agree("2");
    });
    function getUrlParam(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg);  //匹配目标参数
        if (r != null) return unescape(r[2]); return null; //返回参数值
    }
    function agree(cstatus){
        var id=getUrlParam('id');

        var data ={id:id,cstatus:cstatus};
        $.ajax({
            type: 'POST',
            contentType: "application/x-www-form-urlencoded",
            url: "visitorRecord/dealQrcodeUrl",
            data: data,
            success: function(data){
                var obj = JSON.parse(data);

                console.info(data);
                console.info(obj);
                console.info(obj.verify.desc);
                if (obj.verify.sign=="fail"){
                    alert("已拒绝邀约！")
                }else{
                    $(window).attr('location',obj.verify.desc);
                }

            }
        });

    }

</script>
</body>
</html>