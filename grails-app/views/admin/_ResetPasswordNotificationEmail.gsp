<%@ page contentType="text/html" %>
<html>
<head></head>
<body>

Hello ${person},<br>

<p>
Welcome to TransitionManager. A new account has been created for you but will need to activated ASAP. Please click on the following link or copy it into your browser. You will be presented with a secure web page where you be prompted for your email address and you can set you new password.
</p>

${activationURL}

<p>It is important that you activate your account ASAP as the activation period will expire in ${ttl} from when this email was generated.</p>

<p>In the future you can access the TransitionManager website your username will be: ${username}</p>

<p>If you have any technical problems, please contact ${sysAdminEmail}.</p>

<p>-The TransitionManager Team</p>

</body>

</html>