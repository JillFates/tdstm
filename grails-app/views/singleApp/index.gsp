<html>
    <head>
        <base href="${createLink( uri: '/module/' )}">
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <%-- Adding cache expiration to this page --%>
        <meta http-equiv="cache-control" content="max-age=0" />
        <meta http-equiv="cache-control" content="no-cache" />
        <meta http-equiv="expires" content="0" />
        <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
        <meta http-equiv="pragma" content="no-cache" />
        <meta name="layout" content="header"/>
        <title>TransitionManager</title>

        <style>
        /*TODO: REMOVE ON COMPLETE MIGRATION */
        body {
            background-color: #ecf0f5 !important;
        }
        .content-wrapper {
            background-color: #ecf0f5 !important;
        }
        </style>

    </head>

    <body>

        <tds-app>
            <div id="main-loader"><div id="loader-icon"><div class="loader"></div></div></div>
        </tds-app>

        <script src="${resource(dir: 'tds/web-app/dist', file: 'polyfills.js')}?_b=${buildHash}"></script>
        <script src="${resource(dir: 'tds/web-app/dist', file: 'vendor.js')}?_b=${buildHash}"></script>
        <script src="${resource(dir: 'tds/web-app/dist', file: 'app.js')}?_b=${buildHash}"></script>

    </body>

</html>
