<%@ page import="com.tdsops.etl.ETLDomain" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav"/>

    <title>Datasource transformation</title>
    <style type="text/css">
    #script {
        background: url(http://i.imgur.com/2cOaJ.png);
        background-attachment: local;
        background-repeat: no-repeat;
        padding-left: 35px;
        padding-top: 10px;
        border-color: #ccc;
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
                <textarea class="form-control" name="mockData" rows="8" style="width: 100%;">${mockData}</textarea>
            </fieldset>
        </div>

        <div class="col-md-6">
            <fieldset>
                <legend>ETL Scripting Sandbox</legend>
                <br>

                <div>
                    <textarea id="script" class="form-control" name="script" rows=10
                              style="font: normal 10pt Consolas, Monaco, monospace; width: 100%;">${script}</textarea>
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
            <ul class="nav nav-tabs">
                <li class="active"><a data-toggle="tab" href="#home">Results</a></li>
                <li><a data-toggle="tab" href="#menu1">Source</a></li>
            </ul>

            <div class="tab-content">
                <div id="home" class="tab-pane fade in active">


                    <g:each in="${com.tdsops.etl.ETLDomain.values()}" var="domain">
                        <g:set var="domainResults" value="${etlProcessor.results[domain]}"></g:set>

                        <g:if test="${domainResults}">
                            <h3>Results for Domain ${domain}</h3>
                            <table style="width:100%" class="table table-condensed table-hover">
                                <tr>
                                    <g:each in="${domainResults[0]}" var="header">
                                        <th>${header.field.label}</th>
                                    </g:each>
                                </tr>
                                <g:each in="${domainResults}" var="row" status="i">
                                    <tr>
                                        <g:each in="${row}" var="value">
                                            <td>${value.value}</td>
                                        </g:each>
                                    </tr>
                                </g:each>
                            </table>

                        </g:if>
                    </g:each>
                </div>

                <div id="menu1" class="tab-pane fade">
                    <h3>Source</h3>
                    <table style="width:100%" class="table table-condensed table-hover">
                        <tr>
                            <g:each in="${etlProcessor?.columns}" var="column">
                                <th>${column.label}</th>
                            </g:each>
                        </tr>
                        <g:each in="${etlProcessor?.rows}" var="row">
                            <tr>
                                <g:each in="${row.elements}" var="element">
                                    <td>${element.value}</td>
                                </g:each>
                            </tr>
                        </g:each>
                    </table>
                </div>
            </div>
        </div>
    </fieldset>

    <div class="row">
        <div class="col-md-6">
            <g:if test="${logContent}">
                <fieldset>
                    <legend>Console output</legend>
                    <br>
                    <textarea id="console" rows="15"
                              style="background-color: black;color: green; width: 100%;">${logContent}</textarea>
                </fieldset>
            </g:if>
        </div>

        <div class="col-md-6">
            <g:if test="${jsonResult}">
                <fieldset>
                    <legend>JSON result</legend>
                    <br>
                    <textarea id="jsonResult" rows="50"
                              style="font: normal 10pt Consolas, Monaco, monospace; width: 100%;">${jsonResult}</textarea>
                </fieldset>
            </g:if>
        </div>
    </div>
</form>
</body>
</html>