<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta charset="UTF-8">
  <meta name="layout" content="projectHeader" />
  <title>Document</title>
  <style type="text/css">
    .padded {
      padding: 2em;
    }
    .center {      
      text-align: center;
    }
    .big {
      padding: 1em;
    }
    .red {
      color:red;
    }
    .modal{
      position:fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 999;
      background: rgba(255, 255, 255, 0.9);
    }
    .modal > div{
      padding-top: 5em;
    }
    #btnRestart{
      font-size: medium;
    }
  </style>
</head>
<body>
  <h2>
  <g:if test="restartable">
    <div class="padded center red">
      <p class="padded">
        Clicking the Restart button is going to force the application to restart which will cause a brief disruption of service.<br/>
        This action will be recorded.
      </p>
      <button id="btnRestart" type="button" class="big">RESTART</button>      
    </div>
    <script type="text/javascript">
      $(function(){
        $('#btnRestart').on("click", function(e){
          var proceed = confirm("Are you sure you want to restart the application? Click Okay to restart otherwise press Cancel.");
          if(proceed){
            $.post("restartAppService")
            .done(function(){
              $("body").append("<div class='modal'><div class='center'><h1><strong>The Service is Restarting... this could take a while</strong></h1></div></div>");
              setTimeout(function(){
                window.location.replace("${createLink(uri: '/')}");
              }, 60 * 1000);
            });
          }
        });
      });
    </script>
  </g:if><g:else>
    <div class="center red">
      <g:message code="tdstm.admin.serviceRestartCommand.error" />
    </div>
  </g:else>
  </h2>
</body>
</html>