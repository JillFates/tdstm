<%@ page import="com.tdsops.etl.ETLDomain" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav"/>

    <title>Data Ingestion</title>
    <link rel="stylesheet" href="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.css" />
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
                <legend>Test Data Source</legend>
                <br>
                <textarea class="form-control" name="mockData" rows="${lineNumbers - 2}" style="width: 100%;">${mockData}</textarea>
                <br>

                <div class="col-md-12">
                    <h1>Service Now Document</h1>
                    <g:if test="${flash.message}"><div class="message" role="status">${flash.message}</div></g:if>

                        <div class="col-md-8">
                            <input type="text" class="form-control" name="file" />
                        </div>
                        <div class="col-md-4">
                            <g:submitButton name="fetch" class="form-control"  value="Fetch" />
                        </div>

                </div>

            </fieldset>
        </div>

        <div class="col-md-6">
            <fieldset>
                <legend>ETL Scripting Sandbox</legend>
                <br>

                <div>
                    <textarea id="script" class="form-control" name="script" rows="${lineNumbers}" style="font: normal 10pt Consolas, Monaco, monospace; width: 100%;">${script}</textarea>
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
                        <strong>Exception!</strong> ${missingPropertyError} Line number: ${lineNumber}
                    </div>
                </g:if>
                <br>
                <input class="form-control" type="submit" value="Apply">
            </fieldset>
        </div>
    </div>
    <g:if test="${etlProcessor?.columns}"><hr></g:if>
    <g:if test="${etlProcessor?.columns}">
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

                <g:if test="${!(etlProcessor?.results)}">
                    <h2>Note Results yet</h2>
                </g:if>

                    <g:each in="${com.tdsops.etl.ETLDomain.values()}" var="domain">
                        <g:set var="domainResults" value="${etlProcessor?.results?.getAt(domain)}"></g:set>
                        <g:if test="${domainResults}">

                            <h3>Results for Domain ${domain}</h3>

                            <table style="width:100%" class="table table-condensed table-hover">
                                <tr>
                                    <th># Reference</th>
                                    <g:each in="${domainResults[0].elements}" var="header">
                                        <th>${header.field.label}</th>
                                    </g:each>
                                </tr>
                                <g:each in="${domainResults}" var="row" status="i">
                                    <tr>
                                        <td>${row.reference}</td>
                                        <g:each in="${row.elements}" var="value">
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
    </g:if>
    <g:if test="${logContent || jsonResult}"><hr></g:if>
    <div class="row">
        <div class="col-md-${(logContent && !jsonResult)?12:6}">
            <g:if test="${logContent}">
                <fieldset>
                    <legend>Console output</legend>
                    <br>
                    <textarea id="console" rows="15"
                              style="background-color: black;color: green; width: 100%;">${logContent}</textarea>
                    <br>
                </fieldset>
            </g:if>
        </div>

        <div class="col-md-${(!logContent && jsonResult)?12:6}">
            <g:if test="${jsonResult}">
                <fieldset>
                    <legend>JSON result</legend>
                    <br>
                    <div id="json-collasped"></div>
                    %{--<textarea id="jsonResult" rows="50"--}%
                              %{--style="font: normal 10pt Consolas, Monaco, monospace; width: 100%;">--}%
                        %{--${jsonResult.toString(true).stripIndent()}--}%
                    %{--</textarea>--}%
                </fieldset>
            </g:if>
        </div>
    </div>
</form>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.textcomplete/1.8.4/jquery.textcomplete.js"></script>
<script type="text/javascript" src="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.js"></script>
<script>

    $(function() {
        $("#json-collasped").JSONView( ${raw(jsonResult?.toString(true))}, { collapsed: true });
    });

    $('#script').textcomplete([
        {
            id: 'available-methods',
            words: ${raw(availableMethods)},
            match: /\b(\w{1,})$/,
            search: function (term, callback) {
                callback($.map(this.words, function (word) {
                    return word.indexOf(term) === 0 ? word : null;
                }));
            },
            template: function (value) {
                return '<strong style="text-align: left; display: block;">' + value + '</strong>';;
            },
            index: 1,
            replace: function (word) {
                if(word != "iterate") {
                    return word + ' ';
                } else {
                    return word + ' {\n\n\n\n}';
                }

            }
        },
        {
            id: 'asset-fields',
            words: ${raw(assetFields)},
            match: /\b(\w{2,})$/,
            search: function (term, callback) {
                callback($.map(this.words, function (word) {
                    return word.indexOf(term) === 0 ? word : null;
                }));
            },
            index: 1,
            replace: function (word) {
                return word + ' ';
            }
        }
    ], {
        onKeydown: function (e, commands) {
            if (e.ctrlKey && e.keyCode === 74) { // CTRL-J
                return commands.KEY_ENTER;
            }
        }
    });
</script>


</body>
</html>