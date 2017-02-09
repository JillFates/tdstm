<g:set var="TDSResources" value="true" scope="request"/>
<html>
    <head>
        <base href="${createLink( uri: '/app/angular/' )}">
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <meta name="layout" content="header"/>
        <title>Transition Manager</title>
    </head>

    <body>
        <tds-app> Loading TDS App</tds-app>


        <script src="${resource(dir: 'tds/web-app/dist', file: 'vendor.js')}"></script>
        <script src="${resource(dir: 'tds/web-app/dist', file: 'app.js')}"></script>
    </body>

</html>
