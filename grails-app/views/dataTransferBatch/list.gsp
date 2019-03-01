<% def assetClassMap = [ AssetEntity:'device', Application:'app', Database:'db', Files:'files' ] %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>Manage Import Batches (Excel)</title>

        <asset:stylesheet href="css/progressbar.css" />
        <asset:stylesheet href="css/tds-bootstrap.css" />
        <g:render template="/layouts/responsiveAngularResources" />
        <g:javascript src="progressBar.js" />
        <style type="text/css">
          .block-anchor{
            text-align: center;
            width: 70px;
            display: inline-block;
          }
        </style>
        <style>
        /*TODO: REMOVE ON COMPLETE MIGRATION */
        div.content-wrapper {
            background-color: #ecf0f5 !important;
        }
        </style>
    </head>
    <body>
        <tds:subHeader title="Manage Import Batches (Excel)" crumbs="['Assets','Manage Import Batches (Excel)']"/>
        <g:if test="${flash.message}">
            <div class="margin">
                <div class="callout callout-info" style="margin-bottom: 0!important;">
                    <h3 class="icon"><i class="fa fa-info"></i></h3>
                    <div class="info-content-msg">
                        ${flash.message}
                    </div>
                </div>
            </div>
        </g:if>
    <div>
        <div id="messageId" class="message" style="display:none">test</div>
        <div id="progressbar" style="margin-bottom:4px; display: none;" class="centered"></div>
    </div>
    <!-- Main content -->
        <section>
            <div>
                <div class="box-body">
                    <div id="manageAssetImportBatchesList"></div>
                </div>
            </div>
        </section>

        <!-- Modal -->
        <div id="dlgLog" class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
          <div class="modal-dialog" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h5 class="modal-title" id="myModalLabel">Import Results</h5>
              </div>
              <div class="modal-body" style="max-height:20em; overflow-y:auto"></div>
              <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>
          </div>
        </div>
		<asset:stylesheet href="css/progressbar.css" />
		<g:javascript src="jquery/ui.progressbar.js"/>
		<script type="text/javascript">
            function onDataBound() {
                //Manage Import Results Dialog
                $("a.lnkViewLog").on("click", function (e) {
                    e.preventDefault();
                    var uri = $(e.currentTarget).attr("data-link");
                    $.get(uri).done(function (data) {
                        var msg = data.data.importResults
                        if (msg) {
                            $("#dlgLog div.modal-body").html(msg);
                            $("#dlgLog").modal({show: true});
                        }
                    }).fail(function (jqXHR, textStatus, errorThrown) {
                        console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
                        alert('An error occurred while invoking retrieving the information.');
                    });
                });
            }

            var messageDiv = $("#messageId");

            var progressKey = '';
            var messageDiv = $("#messageId");
            var postingFlag = false;	// used to limit one posting at a time
            var progressModal;

            // This method will use Ajax to kickoff the Process function and activate the progress modal
            function kickoffProcess(assetClass, reviewOrProcess, batchId) {
                if (postingFlag) {
                    alert('You can only perform one action at a time.');
                    return false;
                }
                if (reviewOrProcess == 'p') {
                    if (!confirm('Please confirm that you want to post the imported assets to inventory?')) {
                        return false;
                    }
                }
                postingFlag = true;

                messageDiv.html('').hide();

                var title = (reviewOrProcess == 'r' ? 'Reviewing assets in batch ' : 'Posting assets to inventory for batch ') + batchId;
                var uri = '/import/invokeAssetImport' + (reviewOrProcess == 'r' ? 'Review' : 'Process') + '/' + batchId;
                $.post(
                        tdsCommon.createAppURL(uri)
                ).done(function (data) {
                    if (data.status == 'error') {
                        alert(data.errors);
                        console.log('Error: kickoffProcess() : ' + data.errors);
                    } else {
                        var results = data.data.results;
                        progressKey = results.progressKey;  // Used to get the progress updates

                        var progressModal = tds.ui.progressBar(
                                progressKey,
                                5000,
                                function () {
                                    processFinished(assetClass, batchId, reviewOrProcess);
                                },
                                function () {
                                    processFailed(assetClass, batchId, reviewOrProcess);
                                },
                                title
                        );
                    }
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    // stopProgressBar();
                    console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
                    alert('An error occurred while invoking the posting process.');
                }).always(function () {
                    postingFlag = false;

                });

                return false;
            }

            // This is called after a successful batch process that will make Ajax call to get the results of the review or posting results
            function processFinished(assetClass, batchId, reviewOrProcess) {
                var hiddenProcessButton = "#" + assetClass + 'ProcessId_' + batchId;
                var currentButton = $("#" + assetClass + "ReviewId_" + batchId);
                if (reviewOrProcess == 'r') {
                    // Flip the review button over to the Process
                    currentButton.html($(hiddenProcessButton).html());

                } else {
                    currentButton.hide();
                    loadManageAssetImportBatchesList();
                }

                console.log("showProcessResults() was called");

                // Get the status of the batch and update the list accordingly
                $.post(
                        tdsCommon.createAppURL('/import/importResults/' + batchId)
                ).done(function (data) {
                    if (data.status == 'error') {
                        console.log('Error: showProcessResults() : ' + data.errors);
                        $("#statusCode" + batchId).html(results.batchStatusCode);
                    } else {
                        var results = data.data;
                        $("#statusCode" + batchId).html(results.batchStatusCode);
                    }
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
                    alert('An error occurred while getting the posting results.');
                }).always(function () {
                    // allow another action to occur
                    postingFlag = false;

                });
            }

            // This is called when the progress view receives a failure message
            function processFailed(assetClass, batchId, reviewOrProcess) {
                console.log("Progress failed for " + assetClass + " batch " + batchId + " for " + (reviewOrProcess == 'r' ? 'Review' : 'Posting'));
            }

            currentMenuId = "#assetMenu";
            $(".menu-parent-assets-manage-batches").addClass('active');
            $(".menu-parent-assets").addClass('active');
            $('#assetMenu').show();
            $('#reportsMenu').hide();

		</script>
    <script id="actionButtonsTemplate" type="text/x-kendo-template">
        # var showDiv = false; #
        <div class="block-anchor">
            # if (status == 'PENDING' ) { #
                <span id="#:className#ReviewId_#:batchId#">
                    <a href="javascript:" onclick="kickoffProcess('#:className#', 'r', '#:batchId#')">Review</a>
                </span>
                <%--
                    -- Generate the Process button that will be used to replace the Review after the review function is called
                --%>
                <span id="#:className#ProcessId_#:batchId#" style="display: none;" >
                    <a href="javascript:" onclick="return kickoffProcess('#:className#', 'p', '#:batchId#');">Process</a>
                </span>
            # } else { #
                # if (hasErrors == '1') { #
                    <a href="errorsListView?id=#:batchId#">View Errors</a>
                # } else { showDiv = false; } #
            # } #
        </div>
        <span>|</span>
        # if(importResults){ #
            <a href="\\#" data-link="#:contextPath#/dataTransferBatch/importResults/#:batchId#" title="View Log" class="lnkViewLog"><g:img uri="${resource(dir:'icons',file:'script_error.png')}" width="16" height="16" alt="View Log"/></a> |
        # } else { #
            <div style="display:inline-block;width:16px;text-align: center;">-</div>
        # } #
        <a href="#:contextPath#/dataTransferBatch/delete?batchId=#:batchId#" title="Delete Batch">
            %{--TODO what is this?--}%
            <img src="#:contextPath#/static/icons/delete.png" width="16" height="16" alt="Delete Batch">
        </a>
    </script>
    <script type="text/javascript">
        /**
         * Implementing Kendo Grid for Bundle List
         */
        function loadManageAssetImportBatchesList() {
            $("#manageAssetImportBatchesList").kendoGrid({
                toolbar: kendo.template('<div onclick="loadManageAssetImportBatchesList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                dataSource: {
                    type: "json",
                    transport: {
                        read: {
                            // with this param "max=0" we force the endpoint to retreive all batches list
                            // and allow pagination to occur on full client side.
                            url: contextPath + '/DataTransferBatch/retrieveManageBatchList?max=0'
                        }
                    },
                    schema: {
                        model: {
                            fields: {
                                batchId: { type: "string" },
                                importedAt: { type: "date"},
                                importedBy: { type: "string" },
                                attributeSet: { type: "string" },
                                class: { type: "string" },
                                assets: { type: "number" },
                                errors: { type: "string" },
                                status: { type: "string" },
                                action: { type: "string" },
                                className: { type: "string"},
                                hasErrors: { type: "string"},
                                importResults: { type: "boolean" }
                            }
                        }
                    },
                    sort: {
                        field: "importedAt",
                        dir: "desc"
                    }
                },
                columns: [
                    {
                        field: "batchId",
                        title: "Batch Id"
                    },
                    {
                        field: "importedAt",
                        title: "Imported At",
                        format: "{0: " + tdsCommon.kendoDateTimeFormat() + "}"
                    },
                    {
                        field: "importedBy",
                        title: "Imported By"
                    },
                    {
                        field: "attributeSet",
                        title: "Attribute Set"
                    },
                    {
                        field: "class",
                        title: "Class"
                    },
                    {
                        field: "assets",
                        title: "Assets"
                    },
                    {
                        field: "errors",
                        title: "Errors"
                    },
                    {
                        field: "status",
                        title: "Status",

                    },
                    {
                        field: "action",
                        title: "Action",
                        filterable: false,
                        template: kendo.template($("#actionButtonsTemplate").html()),
                        width: 200
                    },
                    {
                        field: "className",
                        hidden: true
                    },
                    {
                        field: "hasErrors",
                        hidden: true
                    },
                    {
                        field: "importResults",
                        hidden: true
                    }
                ],
                sortable: true,
                filterable: {
                    mode: "row"
                },
                pageable: {
					pageSize: ${raw(com.tdsops.common.ui.Pagination.MAX_DEFAULT)},
					pageSizes: [ ${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) } ]
                },
                dataBound: onDataBound
            });
        }

        loadManageAssetImportBatchesList();

    </script>

    </body>
</html>
