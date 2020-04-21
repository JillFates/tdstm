<%@ page contentType="text/html" %>
<html>
<head>
    <link rel="stylesheet" href="https://unpkg.com/@clr/ui/clr-ui.min.css" />
    <!--CLARITY ICONS STYLE-->
    <link rel="stylesheet" href="${resource(dir:'dist/css/clarity',file:'clr-icons.min.css')}">
    <!--CLARITY ICONS API & ALL ICON SETS-->
    <script src="${resource(dir:'dist/js/vendors/clarity',file:'clr-icons.min.js')}"></script>
</head>
<body>

Hi ${person},<br>

<p>The following ETL Import Batch(es) have been processed:</p>

<table style="border-collapse: collapse;margin: 5px 0 5px 2px;width: auto;" class="table clarity-table-grid fixed-header">
    <thead>
        <tr>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Batch ID</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Status</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Domain</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Records</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Processed</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Pending</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Erred</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Ignored</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Inserted</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Updated</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Unchanged</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">Deleted</th>
            <th style="border: 1px solid #c1c7d0;background: #f4f5f7;padding: 6px 8px 5px 8px;text-align: center;">TBD</th>
        </tr>
    </thead>
    <tbody>
        <g:each var="batch" in="${batches}">
            <tr>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">
                    <a href="${serverURL}/module/importbatch/list/${batch.id}">${batch.id}</a>

                </td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.status}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.domain}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.records}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.processed}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.pending}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.erred}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.ignored}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.inserted}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.updated}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.unchanged}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.deleted}</td>
                <td style="border: 1px solid #c1c7d0;padding: 6px 8px 5px 8px;">${batch.tbd}</td>
            </tr>
        </g:each>
    </tbody>
</table>
</body>
</html>
