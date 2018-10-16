<%@ page contentType="text/html" %>
<html>
<head></head>
<body>

<p>The TransitionManager administrator has sent you a Reset Password request.</p>

<p>Please click on the following link or copy it into your browser.  You will be presented with a secure web page where you be prompted for your email address and you can set your new password.</p>

<a href="${resetPasswordUrl}">${resetPasswordUrl}</a><br>

<p>It is important that you activate your account ASAP as the activation period will expire in 60 minutes from when this email was generated.
Once the password is changed you can access the TransitionManager website with your username: ${username} <br>
If you have any technical problems, please contact ${supportEmail}</p>

Regards, <br>
TransitionManager Team
</body>

</html>