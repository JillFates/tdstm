<%@ page contentType="text/html" %>
<html>
<head></head>
<body>

Dear ${person},<br>
<p>
TransitionManager has received a request to reset the password for your account.
To reset your password please click on the following link or copy it into your browser. This will take you to a secure web page where you can change your password. Please note that this reset token will expire in ${expiredTime} minutes from the time it was initiated.
</p>
<a href="${resetPasswordUrl}">Click here to reset your password</a><br>
<p>
If you did not request that your password be reset, you can safely ignore this email. It's likely that another person had mistakenly initiated a password reset referencing your email. As long as you do not click the link above, no action will be taken and your account will remain secure and unchanged.
For concerns or technical problems, please contact ${supportEmail}.
</p>
-The TransitionManager Team

</body>

</html>
