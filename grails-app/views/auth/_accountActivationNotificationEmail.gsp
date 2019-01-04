<%@page expressionCodec="none" %>
<%@ page contentType="text/html" %>
<html>
<head></head>
<body>

Hi ${person},<br>
<p>
${customMessage}
</p>

<p>A new account has been created for you but will need to be activated ASAP. Please click on the following link or copy it into your browser. You will be presented with a secure web page where you can then set your password. Going forward your username for accessing TransitionManager will be '${username}'.</p>
<p>${activationURL}</p>
<p>It is important that you activate your account ASAP as the activation period will expire in ${ttl} hours from when this email was generated.
After activating your user account you may access the website anytime at the following URL:</p>
${serverURL}
<p>If you have any technical problems, please contact ${sysAdminEmail}.</p>

<p>-The TransitionManager Team</p>

</body>

</html>
