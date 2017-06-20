<html>
    <head>
        <base href="${createLink( uri: '/module/' )}">
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <meta name="layout" content="header"/>
        <title>Transition Manager</title>

        <style>
        /*TODO: REMOVE ON COMPLETE MIGRATION */
        .content-wrapper {
            background-color: #ecf0f5 !important;
        }
        </style>

    </head>

    <body>

        <ui-view><div id="main-loader"><div id="loader-icon"><div class="loader"></div></div></div></ui-view>

        <script src="${resource(dir: 'tds/web-app/dist', file: 'vendor.js')}"></script>
        <script src="${resource(dir: 'tds/web-app/dist', file: 'app.js')}"></script>

    </body>

</html>
