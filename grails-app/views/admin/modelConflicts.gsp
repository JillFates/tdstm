<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="topNav" />
        <title>Model / Alias Conflicts</title>


        <style type="text/css">
        body{
            font-family:'helvetica','arial';
        }
        th {
            text-align: center;
        }
        tbody tr td {
            text-align: center;
            line-height: 6px;
        }
        table{
            border-spacing: 5px;
            border-collapse: separate;
            /*border: 1px solid black;*/
            table-layout: fixed;
            width: auto;
        }
        </style>

    </head>

    <body>

        <tds:subHeader title="Model / Alias Conflicts" crumbs="['Admin','Model / Alias Conflicts']"/>
        <div class="body">
            <h1><b>Conflicts</b></h1>
            <table>
                    <thead>
                        <tr>
                            <th>Manufacturer</th>
                            <th>Model Id</th>
                            <th>Model Name</th>
                            <th>Alternate Model</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${conflictList}" var="row" status="i">
                            <tr class="${ (i % 2) == 0 ? 'even' : 'odd'}">
                                <td>${row.mfg}</td>
                                <td>${row.model_id}</td>
                                <td>${row.model_name}</td>
                                <td>${row.alternate_model}</td>
                            </tr>
                        </g:each>
                    </tbody>
            </table>
        </div>

    </body>

</html>
