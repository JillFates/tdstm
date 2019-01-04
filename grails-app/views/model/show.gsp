<html>
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="topNav" />
  <title>Model - Show</title>
  </head>
  <body>
<div class="body">
<h1>Model - Show</h1>
<g:if test="${flash.message}">
     <div class="message">${flash.message}</div>
</g:if>
<g:render template="/model/show" />
</div>
  <script>
    $(document).ready(function(){
        localStorage.setItem('editAsset',Date.now());
    });
  </script>
</body>
</html>
