<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="topNav" />

    <title>Datasource transformation</title>
    <style type="text/css">
        #script {
            background: url(http://i.imgur.com/2cOaJ.png);
            background-attachment: local;
            background-repeat: no-repeat;
            padding-left: 35px;
            padding-top: 10px;
            border-color:#ccc;
        }
        samp {
            background: #000;
            border: 3px groove #ccc;
            color: #058907;
            display: block;
            padding: 5px;
        }
    </style>
</head>

<body>

<form method="post">

    <div class="row" class="form-group">
        <div class="col-md-6">
            <fieldset>
                <legend>Mock Data</legend>
                <br>
                <textarea class="form-control" name="mockData" rows="8" style="width: 100%;">
${mockData}
                </textarea>
            </fieldset>
        </div>
        <div class="col-md-6">
            <fieldset>
                <legend>ETL Scripting Sandbox</legend>
                <br>
                <div>
                    <textarea id="script" class="form-control" name="script" rows=10 style="font: normal 10pt Consolas, Monaco, monospace; width: 100%;">
${script}
                    </textarea>
                </div>
                <br>
                <g:if test="${errorCollector}">
                    <div class="alert alert-danger">
                        <strong>${errorCollector.getErrorCount()} Errors!</strong>
                        <g:each in="${errorCollector.errors}" var="error">
                            <th>${error.cause*.message}</th>
                        </g:each>


                    </div>
                </g:if>
                <g:if test="${missingPropertyError}">
                    <div class="alert alert-danger">
                        <strong>Missing property Exception!</strong> ${missingPropertyError} Line number: ${lineNumber}
                    </div>
                </g:if>
                <br>
                <input class="form-control" type="submit" class="btn-primary" value="Apply">
            </fieldset>
        </div>
    </div>

    <fieldset>
        <legend>Raw data modified</legend>
        <br>
        <div>
            <table style="width:100%" class="table table-condensed table-hover">
                %{--<tr>--}%
                    %{--<g:each in="${etlProcessor?.columnNames}" var="columnName">--}%
                        %{--<th>${columnName}</th>--}%
                    %{--</g:each>--}%
                %{--</tr>--}%
                %{--<g:each in="${etlProcessor?.rows()}" var="row">--}%
                    %{--<tr>--}%
                    %{--<g:each in="${row}" var="value">--}%
                        %{--<td>${value}</td>--}%
                    %{--</g:each>--}%
                    %{--</tr>--}%
                %{--</g:each>--}%
            </table>
            %{--<table style="width:100%" class="table">--}%
                %{--<tr>--}%
                    %{--<g:each in="${etlProcessor?.tableHeaders}" var="columnName">--}%
                        %{--<th>${columnName}</th>--}%
                    %{--</g:each>--}%
                %{--</tr>--}%
                %{--<g:each in="${etlProcessor?.tableRows}" var="row">--}%
                    %{--<tr>--}%
                        %{--<g:each in="${row}" var="value">--}%
                            %{--<td>${value}</td>--}%
                        %{--</g:each>--}%
                    %{--</tr>--}%
                %{--</g:each>--}%
            %{--</table>--}%
        </div>
    </fieldset>

        <div class="row">
            <div class="col-md-6">
                <g:if test="${logContent}">
                    <fieldset>
                        <legend>Console output</legend>
                        <br>
                        <textarea id="console" rows="15" style="background-color: black;color: green; width: 100%;">${logContent}</textarea>
                    </fieldset>
                </g:if>
            </div>
            <div class="col-md-6">
                <g:if test="${jsonResult}">
                    <fieldset>
                        <legend>JSON result</legend>
                        <br>
                        <textarea id="jsonResult" rows="50" style="font: normal 10pt Consolas, Monaco, monospace; width: 100%;">${jsonResult}</textarea>
                    </fieldset>
                </g:if>
            </div>
        </div>
    </form>
</body>
</html>