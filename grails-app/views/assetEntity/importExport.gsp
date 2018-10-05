<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav" />
    <title>Import Assets (TM Excel)</title>
    <asset:stylesheet href="css/progressbar.css" />
    <g:javascript src="jquery/ui.progressbar.js"/>
    <g:javascript src="import.export.js"/>
    <style>
    /*TODO: REMOVE ON COMPLETE MIGRATION */
    div.content-wrapper {
        background-color: #ecf0f5 !important;
    }
    </style>
    <script type="text/javascript">
        /*
         * used to show the Progress bar
         */
        var handle=0;
        var requestCount=0;
        var buttonClicked=false;

        function showProcessBar(e) {
            var progress = eval('(' + e.responseText + ')');
            if (progress) {
                $("#progressbar").reportprogress(progress[0].imported,progress[0].total);
                if (progress[0].imported==progress[0].total){
                    clearInterval(handle);
                }
            }
        }

        /*
         * Used to set the interval to display Progress
         */
        jQuery(function($) {
            $("#run").click(function() {
                if ($("#file").val().length == 0) {
                    alert("Please select a file to import.")
                    return false;
                }
                if (buttonClicked) {
                    alert('You already clicked the Import Spreadsheet button and the upload is being processed.');
                    return false;
                }
                buttonClicked=true;
                // Some reason if the button gets disabled it prevents the original click process to continue, we should switch to the JQuery Once function
                //$('#run').prop('disabled',true);

                var progressBar = $("#progressbar");
                progressBar.reportprogress(0, 0, 'Uploading &amp; verifying spreadsheet...');
                progressBar.css("display","block");
                clearInterval(handle);
                if (${isMSIE}) {
                    handle = setInterval("${remoteFunction(action:'retrieveProgress', onComplete:'showProcessBar(XMLHttpRequest)')}", 5000);
                } else {
                    // Increased interval by 5 sec as server was hanging over chrome with quick server request.
                    handle=setInterval(getProgress, 5000);
                }
            });
        });

        //This code is used to display progress bar at chrome as Chrome browser cancel all ajax request while uploading .
        function getProgress(){
            var hiddenVal=$("#requestCount").val()
            if(hiddenVal != requestCount){
                requestCount = hiddenVal
                $("#iFrame").attr('src', contextPath+'/assetEntity/retrieveProgress');
            }
        }

        // Used to handle the AJax response for the progress update
        function onIFrameLoad() {
            var serverResponse = $("#iFrame").contents().find("pre").html();
            var jsonProgress
            if(serverResponse){
                $("#requestCount").val(parseInt(requestCount)+1)
                jsonProgress = JSON.parse( serverResponse )
            }
            if (jsonProgress) {
                var progressBar = $("#progressbar");
                progressBar.reportprogress(jsonProgress[0].imported,jsonProgress[0].total);
                if(jsonProgress[0].imported==jsonProgress[0].total){
                    clearInterval(handle);
                }
            }
        }
    </script>

</head>
<body>
<tds:subHeader title="Import Assets (TM Excel)" crumbs="['Assets','Import Assets (TM Excel)']"/>
<g:if test="${flash.error}">
    <div class="errors">${flash.error}</div>
</g:if>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<g:if test="${error}">
    <div class="margin">
        <div class="callout callout-warning" style="margin-bottom: 0!important;">
            <h3 class="icon"><i class="fa fa-info"></i></h3>
            <div class="info-content-msg">
                ${raw(error)}
            </div>
        </div>
    </div>
</g:if>
<g:if test="${message}">
    <div class="margin">
        <div class="callout callout-info" style="margin-bottom: 0!important;">
            <h3 class="icon"><i class="fa fa-info"></i></h3>
            <div class="info-content-msg">
                ${raw(message)}
            </div>
        </div>
    </div>
</g:if>
<!-- Main content -->
<section>
    <div>
        <div class="box-body">
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title">Import</h3>
                </div><!-- /.box-header -->
                <g:form action="upload" method="post" name="importForm" enctype="multipart/form-data" role="form" class="medium-size">
                    <iframe id='iFrame' class="iFrame" onload='onIFrameLoad()'></iframe>
                    <g:hiddenField name="requestCount" id="requestCount" value="1"/>
                    <div class="box-body">
                        <div class="form-group" style="display: none;">
                            <label for="dataTransferSet">Import Type:</label>
                            <select id="dataTransferSet" name="dataTransferSet" class="form-control">
                                <g:each status="i" in="${dataTransferSetImport}" var="dataTransferSet">
                                    <option value="${dataTransferSet?.id}">${dataTransferSet?.title}</option>
                                </g:each>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="uploadFile">File input</label>
                            <input id="uploadFile" class="form-control" placeholder="Choose File" disabled="disabled" />
                            <div class="fileUpload btn btn-default">
                                <span>Select</span>
                                <input id="file" type="file" class="upload" name="file" />
                            </div>
                            <div id="progressbar" style="display: none;" ></div>
                        </div>

                        <div class="form-group">
                            <label for="checkboxGroup">Choose Items to Import:</label>
                            <div class="row checkboxGroup" id="checkboxGroup">
                                <div class="col-md-6">
                                    <ul class="list-group sub-set">
                                        <li class="list-group-item">
                                            <input type="checkbox" id="applicationId" name="application" value="application"
                                                   onclick="importExportPreference($(this),'ImportApplication')"
                                                ${prefMap['ImportApplication']=='true' ? 'checked="checked"' :''}/>
                                            &nbsp;
                                            <label for="applicationId">Application</label>
                                        </li>
                                        <li class="list-group-item">
                                            <input type="checkbox" id="assetId" name="asset" value="asset"
                                                   onclick="importExportPreference($(this),'ImportServer')"
                                                ${prefMap['ImportServer'] =='true'? 'checked="checked"' :''}/>
                                            &nbsp;
                                            <label for="assetId">Devices</label>
                                        </li>
                                        <li class="list-group-item">
                                            <input type="checkbox" id="databaseId" name="database" value="database"
                                                   onclick="importExportPreference($(this),'ImportDatabase')"
                                                ${prefMap['ImportDatabase'] =='true' ? 'checked="checked"' :''}/>
                                            &nbsp;
                                            <label for="databaseId">Database</label>
                                        </li>
                                        <li class="list-group-item">
                                            <input type="checkbox" id="storageId" name="storage" value="storage"
                                                   onclick="importExportPreference($(this),'ImportStorage')"
                                                ${prefMap['ImportStorage']=='true' ? 'checked="checked"' :''}/>
                                            &nbsp;
                                            <label for="storageId">Storage</label>
                                        </li>
                                    </ul>
                                </div>
                                <div class="col-md-6">
                                    <ul class="list-group sub-set">
                                        <li class="list-group-item"><label class="sub-set-title">These post immediately:</label></li>
                                        <li class="list-group-item">
                                            <input type="checkbox" id="dependencyId" name="dependency" value="dependency"
                                                   onclick="importExportPreference($(this),'ImportDependency')"
                                                ${prefMap['ImportDependency'] =='true' ? 'checked="checked"' :''}/>
                                            &nbsp;
                                            <label for="dependencyId">Dependency</label>
                                        </li>
                                        <li class="list-group-item">
                                            <input type="checkbox" id="cablingId" name="cabling" value="cable"
                                                   onclick="importExportPreference($(this),'ImportCabling')"
                                                ${prefMap['ImportCabling']=='true' ? 'checked="checked"' :''}/>
                                            &nbsp;
                                            <label for="cablingId">Cabling</label>
                                        </li>
                                        <li class="list-group-item">
                                            <input type="checkbox" id="commentId" name="comment" value="comment"
                                                   onclick="importExportPreference($(this),'ImportComment')"
                                                ${prefMap['ImportComment']=='true' ? 'checked="checked"' :''}/>
                                            &nbsp;
                                            <label for="commentId">Comment</label>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div><!-- /.box-body -->

                    <tds:hasPermission permission="${Permission.AssetImport}">
                        <div class="box-footer">
                            <button type="submit" id="run"  class="btn btn-primary">Import Spreadsheet&nbsp;<span class="exportIcon glyphicon glyphicon-download" aria-hidden="true"></span></button>
                            <div class="box-footer-url-right"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> <g:link controller="dataTransferBatch" >Manage Import Batches (Excel): ${dataTransferBatchs}</g:link></div>
                        </div>
                    </tds:hasPermission>
                </g:form>


            </div>
        </div>
        <script>
            currentMenuId = "#assetMenu";
            $(".menu-parent-assets-import-assets").addClass('active');
            $(".menu-parent-assets").addClass('active');
            $(document).ready(function() {
                $("#file").on('change', function() {
                    var fileName  = $(this).val().split(/(\\|\/)/g).pop();
                    var fileExt = fileName.split('.').pop()
                    if(["xls", "xlsx"].indexOf(fileExt) >= 0){
                        $("#uploadFile").val(fileName);
                        $("#importTaskSubmitButton").attr('disabled', false)
                    }else{
                        $(this).val(null)
                        $("#uploadFile").val(fileName);
                        $("#importTaskSubmitButton").attr('disabled', true)
                        alert("Please, select a valid file.")
                    }

                });
            });
        </script>
    </div>
    <!-- /.box-body -->
</div>
</section>
</body>
</html>
