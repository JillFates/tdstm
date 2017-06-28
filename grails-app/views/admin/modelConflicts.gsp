<%--
  Created by IntelliJ IDEA.
  User: David Ontiveros
  Date: 27/06/2017
  Time: 11:23 PM
--%>

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
        }
        table{
            /*border-spacing: 5px;
            border-collapse: separate;*/
            border: 1px solid black;
            table-layout: fixed;
            width: auto;
        }
        </style>
    </head>

    <body>

        <tds:subHeader title="Model / Alias Conflicts" crumbs="['Admin','Model / Alias Conflicts']"/>

        <div class="body">

            <table>
                    <thead>
                        <tr>
                            <th>Manufacturer</th>
                            <th>Model Id</th>
                            <th>Model Name</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${conflictList}" var="row">
                            <tr>
                                <td>${row.mfg}</td>
                                <td>${row.model_id}</td>
                                <td>${row.model_name}</td>
                            </tr>
                        </g:each>
                    </tbody>
            </table>

        </div>

    </body>

</html>