<%@ page contentType="text/html" %>
<html>
<head></head>
<body>

Hi ${person},<br>

<p>The ETL process you initiated encountered an error that prevented the process from completing.</p><p>The information below may provide the necessary information to correct the error. If not, please forward this email to support.</p>
<table style="border-collapse: collapse;margin: 5px 0 5px 2px;width: auto;">
    <tbody>
    </tr>
    <tr>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">TransitionManager URL:</td>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${serverURL}</td>
    </tr>
    <tr>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">Project:</td>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${projectName}</td>
    </tr>
    <tr>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">Provider</td>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${providerName}</td>
    </tr>
    <tr>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">ETL Script:</td>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${dataScriptName}</td>
    </tr>
    <tr>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">Group ID:</td>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${groupGuid}</td>
    </tr>
    <tr>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">Failed During:</td>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${failedDuring}</td>
    </tr>
    <tr>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">Error:</td>
        <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${errorMessage}</td>
    </tr>
    </tbody>
</table>
</body>
</html>
