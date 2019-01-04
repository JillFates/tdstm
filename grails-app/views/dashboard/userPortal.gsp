<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="layout" content="topNav"/>
    <title>User Dashboard For ${loggedInPerson}</title>

    <asset:stylesheet src="css/dashboard.css" />
    <asset:stylesheet src="css/tabcontent.css" />
    <asset:stylesheet src="css/userPortal.css" />
    <asset:stylesheet src="css/ui.datepicker.css" />

    <g:javascript src="asset.comment.js"/>
    <g:javascript src="asset.tranman.js"/>
    <g:javascript src="entity.crud.js"/>
    <g:render template="/layouts/responsiveAngularResources"/>
    <g:javascript src="model.manufacturer.js"/>

    <style>
        body {
            /* any change like this MUST be made always inside a wrapper, never in a basic html tag, this will reset the default behave */
            text-align: inherit !important;
        }

        .panel-default {
            margin-top: 22px;
            font-weight: bold;
        }

        #project-selector {
            margin-top: 5px;
        }

        .cell-container {
            margin-top: 20px;
        }

        .btn-refresh {
            float: right;
            cursor: pointer;
        }

        .k-selectable {
            cursor: pointer;
        }

        #taskSummaryDetail {
            margin-top: 10px;
        }

        .task_overdue {
            color: #FF6B6B;
        }

        .taskTd {
            font-weight: bold;
            font-size: 12px;
        }

        .statusButtonBar {
            background: none;
            box-shadow: none;
            padding-left: 0px;
        }

        a.task_action {
            margin: 0 0 0 16px;
        }

        .glyphicon {
            position: relative;
            top: 1px;
            display: inline-block;
            font-family: 'Glyphicons Halflings';
            -webkit-font-smoothing: antialiased;
            font-style: normal;
            font-weight: normal;
            line-height: 1;
            -moz-osx-font-smoothing: grayscale;
        }

        .glyphicon-refresh:before {
            content: "\e031";
        }

        .custom-action {
            padding-left: 8px !important;
        }

        .custom-action .k-icon {
            background-color: white !important;
            background-color: transparent;
            border-radius: 50%;
        }

        .k-grid {
            margin-top: 24px;
        }

</style>

</head>

<body>
<tds:subHeader title="User Dashboard for ${loggedInPerson}" crumbs="['Dashboard','User']"/>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <a name="page_up"></a>

    <div id="doc" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">

        <script id="taskRowTemplate" type="text/x-kendo-tmpl">

            <tr id="issueTrId_#: taskId #" data-uid="#: uid #" class="#: css #">
                <td class="custom-action" style="cursor: pointer; outline: none;" onclick="changeDropSummary(this)" href="\\#" tabindex="-1" action-bar-cell config-table="config.table" comment-id="#: taskId #" asset-id="#: assetId #" status="#: status #" id-prefix="issueTrId_" master="true" table-col-span="5">
                    <a class="k-icon k-plus"></a>
                </td>
                <td #:isAllProjectMode() ? '' : 'style=display:none' # class="taskTd">
                    #: projectName #
                </td>
                <td class="taskTd">
                    #: task #
                </td>
                <td class="taskTd" style="cursor: pointer;">
                    <span onclick="EntityCrud.showAssetDetailView('#: assetClass #', '#: assetId #')"> #: related # </span>
                </td class="taskTd">
                <td class="#: overDue # taskTd">
                    #: dueEstFinish #
                </td>
                <td class="taskTd">
                    #: status #
                </td>
            </tr>
        </script>

        <!-- Body Starts here-->
        <div id="bodycontent">

            <div class="panel panel-default">
                <div class="panel-heading">
                    Project:&nbsp;<g:select id="userProjectId" name="projectId" from="${projects}" optionKey="id" optionValue="name" value="${projectInstance.id}" style="width: 250px;"/>
                </div>

                <div class="panel-body" style="margin-top: -20px;">
                    <div class="container-fluid">
                        <div class="row">
                            <div class="col-md-6 cell-container">
                                <div id="gridEvents"></div>

                                <div id="gridTaskSummary"></div>
                                <div id="taskSummaryDetail"></div>

                            </div>

                            <div class="col-md-6 cell-container">
                                <div id="gridEventsNews"></div>

                                <div id="gridApplication"></div>

                                <div id="gridActivePeople"></div>
                            </div>
                        </div>

                    </div>
                </div>

            </div>
        </div>

        <div id="relatedEntitiesId"></div>

        <g:render template="/assetEntity/initAssetEntityData"/>
    </div>
    <script type="text/javascript">

        var image = '<tr><td><div><asset:image src="images/processing.gif" /></div></td></tr>';
        currentMenuId = "#teamMenuId";

        function changeDropSummary(e) {
            var iconAnchor;
            if(e) {
                iconAnchor = ($(e).hasClass('k-plus') || $(e).hasClass('k-minus'))
                if(iconAnchor) {
                    if($(e).hasClass('k-plus')) {
                        $(e).removeClass('k-plus').addClass('k-minus');
                    } else if($(e).hasClass('k-minus')) {
                        $(e).removeClass('k-minus').addClass('k-plus');
                    }
                } else if($(e).hasClass('custom-action')) {
                    changeDropSummary($(e).find('a'));
                }
            }
        }

        function issueDetails(id, status) {
            // hideStatus(id,status)
            jQuery.ajax({
                url: contextPath + '/task/showIssue',
                data: {'issueId': id},
                type: 'POST',
                success: function (data) {
                    $('#showStatusId_' + id).css('display', 'none')
                    //$('#issueTr_'+id).attr('onClick','cancelButton('+id+',"'+status+'")');
                    $('#detailId_' + id).html(data)
                    $('#detailTdId_' + id).css('display', 'table-row')
                    //$('#detailId_'+id).css('display','block')
                    $('#taskLinkId').removeClass('mobselect')
                    new Ajax.Request('/assetEntity/updateStatusSelect?id=' + id, {
                        asynchronous: false, evalScripts: true,
                        onComplete: function (e) {
                            var resp = e.responseText;
                            resp = resp.replace("statusEditId", "statusEditId_" + id).replace("showResolve(this.value)", "showResolve()")
                            $('#statusEditTrId_' + id).html(resp)
                            // $('#statusEditId_'+id).val(status)
                        }
                    })
                    $("#labelQuantity").focus();
                }
            });
        }

        function showAssetCommentMyTasks(id) {
            $('#dependencyBox').css('display', 'table');
            jQuery.ajax({
                url: contextPath + '/assetEntity/showComment',
                data: {'id': id},
                type: 'POST',
                success: function (data) {
                    var ac = data[0];
                    $('#predecessorShowTd').html(ac.predecessorTable)
                    $('#successorShowTd').html(ac.successorTable)
                    $('#assignedToTdId').html(ac.assignedTo)
                    $('#estStartShowId').html(ac.etStart)
                    $('#estFinishShowId').html(ac.etFinish)
                    $('#actStartShowId').html(ac.atStart)
                    $('#actFinishShowId').html(ac.dtResolved)
                    $('#dueDateId').html(ac.dueDate)
                    ac = ac.assetComment;
                    $('#statusShowId').attr("class", "task_" + ac.status.toLowerCase())
                    $('#showCommentTable #statusShowId').attr("class", "task_" + ac.status.toLowerCase())
                    $('#commentTdId_myTasks').html(ac.taskNumber + ":" + ac.comment)
                    $('#commentTdId1').html(ac.comment)
                    $('#statusShowId').html(ac.status)
                    $('#showCommentTable #statusShowId').html(ac.status)
                    $('#roleTdId').html(ac.role)
                    $('#hardAssignedShow').html(ac.hardAssigned)
                    $('#durationShowId').html(ac.duration)
                    $('#durationScale').html(ac.durationScale)
                    $('#priorityShowId').html(ac.priority)
                    $('#assetShowValueId').html(ac.assetEntity)
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert("An unexpected error occurred when showing comments.")
                }
            });
        }

        function closeBox() {
            $('#dependencyBox').css("display", "none");
        }

        function cancelButton(id) {
            $('#detailTdId_' + id).css('display', 'none')
            $('#taskLinkId').addClass('mobselect')
            $('#showStatusId_' + id).css('display', 'table-row')
        }

        function changeAction() {
            document.issueAssetForm.action = 'listTasks'
        }

        function retainAction() {
            document.issueAssetForm.action = 'showIssue'
        }

        function pageRefresh() {
            document.issueAssetForm.action = 'listTasks'
            document.issueAssetForm.submit()
        }

        function loadRelatedEntities(id) {
            jQuery.ajax({
                url: contextPath + '/dashboard/retrieveRelatedEntities',
                data: {'project': id ? id : $("#userProjectId").val()},
                type: 'POST',
                success: function (data) {
                    $("#relatedEntitiesId").html(data);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert("An unexpected error occurred when updating entities.")
                }
            });
        }

        function loadEventTable(id) {

            var projectId = id ? id : $("#userProjectId").val();

            var grid = $("#gridEvents").data("kendoGrid");
            if(grid) {
                if(projectId == 0) {
                    grid.showColumn(1);
                }

                if(projectId != 0) {
                    grid.hideColumn(1);
                }
            }

            $("#gridEvents").kendoGrid({
                toolbar: kendo.template('<strong><asset:image src="icons/calendar.png" /> Events</strong> - Your assigned events and your team <div onclick="loadEventTable()" class="btn-refresh"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                dataSource: {
                    type: "json",
                    transport: {
                        read: {
                            url: contextPath + '/dashboard/retrieveEventsList',
                            data: { project:  projectId }
                        }
                    },
                    schema: {
                        model: {
                            fields: {
                                eventId: { type: "number"},
                                projectName: { type: "string"},
                                name: { type: "string" },
                                startDate: { type: "date" },
                                days: { type: "string" },
                                teams: { type: "string" }
                            }
                        }
                    }
                },
                columns: [
                    {
                        field: "eventId",
                        hidden: true,
                    },
                    {
                        field: "projectName",
                        title: "Project Name",
                        hidden: projectId != 0,
                    },
                    {
                        field: "name",
                        template: '<a href="index?moveEvent=#=eventId#">#=name#</a>'
                    },
                    {
                        field: "startDate",
                        title: "Start Date",
                        template:"#= moment(startDate).format(tdsCommon.defaultDateFormat())#"
                    }, {
                        field: "days",
                        title: "Days"
                    }, {
                        field: "teams",
                        title: "Teams"
                    }
                ]
            });
        }

        function loadEventNewsTable(id) {
            var projectId = id ? id : $("#userProjectId").val();

            var grid = $("#gridEventsNews").data("kendoGrid");
            if(grid) {
                if(projectId == 0) {
                    grid.showColumn(1);
                }

                if(projectId != 0) {
                    grid.hideColumn(1);
                }
            }

            $("#gridEventsNews").kendoGrid({
                toolbar: kendo.template('<strong> <asset:image src="icons/newspaper.png" /> Event News</strong> - Active news for your events <div onclick="loadEventNewsTable()" class="btn-refresh"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                dataSource: {
                    type: "json",
                    transport: {
                        read: {
                            url: contextPath + '/dashboard/retrieveEventsNewsList',
                            data: { project:  projectId }
                        }
                    },
                    schema: {
                        model: {
                            fields: {
                                eventId: { type: "number"},
                                projectName: { type: "string"},
                                date: { type: "date" },
                                event: { type: "string" },
                                news: { type: "string" }
                            }
                        }
                    }
                },
                columns: [
                    {
                        field: "eventId",
                        hidden: true,
                    },
                    {
                        field: "projectName",
                        title: "Project Name",
                        hidden: projectId != 0,
                    },
                    {
                        field: "date",
                        title: "Date",
                        template:"#= moment(date).format(tdsCommon.defaultDateTimeFormat())#"
                    }, {
                        field: "event",
                        title: "Event",
                        template: '<a href="index?moveEvent=#=eventId#">#=event#</a>',
                    }, {
                        field: "news",
                        title: "News"
                    }
                ]
            });
        }

        function loadTasksTable(id) {


            var projectId = id ? id : $("#userProjectId").val();

            var grid = $("#gridTaskSummary").data("kendoGrid");
            if(grid) {
                if(projectId == 0) {
                    grid.showColumn(0);
                }

                if(projectId != 0) {
                    grid.hideColumn(0);
                }
            }

            //$('#gridTaskSummary').find('.k-grid-content table').remove();

            // do the fact that Task Summary can handle even more information that we expect for Kendo, create on this way
            var dataSource = new kendo.data.DataSource({
                transport: {
                    read: function(options) {
                        $.ajax({
                            type: "POST",
                            url: contextPath + '/dashboard/retrieveTaskSummaryList',
                            dataType: 'json',
                            data: {'project': projectId},
                            success: function(data) {
                                options.success(data.taskList);
                                $('#taskSummaryDetail').html(data.summaryDetail);
                            }
                        });
                    },
                    schema: {
                        model: {
                            fields: {
                                projectName: { type: "string" },
                                task: { type: "string" },
                                assetClass: { type: "string" },
                                assetId: { type: "number" },
                                related: { type: "string" },
                                dueEstFinish: { type: "string" },
                                status: { type: "string" }
                            }
                        }
                    }
                }
            });

            $("#gridTaskSummary").kendoGrid({
                toolbar: kendo.template('<strong><asset:image src="icons/table.png" /> Task Summary</strong> - Active tasks assigned to you <div onclick="loadTasksTable()" class="btn-refresh"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                dataSource: dataSource,
                columns: [
                    {
                        field: "projectName",
                        title: "Project Name",
                        hidden: projectId != 0
                    },
                    {
                        field: "task",
                        title: "Task"
                    },
                    {
                        field: "assetClass",
                        hidden: true
                    },
                    {
                        field: "assetId",
                        hidden: true
                    },
                    {
                        field: "related",
                        title: "Related",
                    }, {
                        field: "dueEstFinish",
                        title: "Due/Est Finish",
                        width: 140
                    }, {
                        field: "status",
                        title: "Status",
                        width: 80
                    }
                ],
                rowTemplate: kendo.template($("#taskRowTemplate").html()),
                detailInit: function(e) {
                    var detailRow = e.detailRow;
                    detailRow.find('td').css('padding', 0);
                },
                dataBound: function(e) {
                    recompileDOM("gridTaskSummary  .custom-action");
                },
                height: 400
            });

        }

        function loadAppTable(id) {
            var projectId = id ? id : $("#userProjectId").val();

            var grid = $("#gridApplication").data("kendoGrid");
            if(grid) {
                if(projectId == 0) {
                    grid.showColumn(0);
                }

                if(projectId != 0) {
                    grid.hideColumn(0);
                }
            }

            $("#gridApplication").kendoGrid({
                toolbar: kendo.template("<strong><div id='appIcon' style='float:left; padding-right: 3px;'></div> Application</strong> - Your applications as a SME or Owner <div onclick='loadAppTable()' class='btn-refresh'><span class='glyphicon glyphicon-refresh' aria-hidden='true'></span></div>"),
                dataSource: {
                    type: "json",
                    transport: {
                        read: {
                            url: contextPath + '/dashboard/retrieveApplicationsList',
                            data: { project:  projectId }
                        }
                    },
                    schema: {
                        model: {
                            fields: {
                                projectName: { type: "string"},
                                name: { type: "string" },
                                appId: { type: "number"},
                                assetClass: { type: "string"},
                                planStatus: { type: "string" },
                                moveBundle: { type: "string" },
                                relation: { type: "string" }
                            }
                        }
                    }
                },
                columns: [
                    {
                        field: "projectName",
                        title: "Project Name",
                        hidden: projectId != 0,
                    },
                    {
                        field: "name",
                        title: "Name"
                    },
                    {
                        field: "appId",
                        hidden: true,
                    },
                    {
                        field: "assetClass",
                        hidden: true,
                    },
                    {
                        field: "planStatus",
                        title: "Plan Status"
                    },
                    {
                        field: "moveBundle",
                        title: "Bundle"
                    },
                    {
                        field: "relation",
                        title: "Relation"
                    }
                ],
                selectable: "row",
                change: onChange
            });

            function onChange() {
                if(this.select) {
                    var selectedItem = $("#gridApplication").data("kendoGrid").dataItem(this.select());
                    EntityCrud.showAssetDetailView(selectedItem.assetClass, selectedItem.appId)
                }
            }

            $('#appIcon').html("<tds:svgIcon name='application_menu' width='18' height='18' />");
        }

        function loadActivepplTable(id) {
            var projectId = id ? id : $("#userProjectId").val();

            $("#gridActivePeople").kendoGrid({
                toolbar: kendo.template('<strong><asset:image src="icons/group.png" /> Active People</strong> - Currently active people on this project <div onclick="loadActivepplTable()" class="btn-refresh"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                dataSource: {
                    type: "json",
                    transport: {
                        read: {
                            url: contextPath + '/dashboard/retrieveActivePeopleList',
                            data: { project:  projectId }
                        }
                    },
                    schema: {
                        model: {
                            fields: {
                                personId: { type: "string" },
                                projectName: { type: "string" },
                                personName: { type: "string" },
                                lastActivity: { type: "string" }
                            }
                        }
                    }
                },
                columns: [
                    {
                        field: "personId",
                        hidden: true
                    },
                    {
                        field: "projectName",
                        title: "Project"
                    },
                    {
                        field: "personName",
                        title: "Name"
                    },
                    {
                        field: "lastActivity",
                        title: "Latest Activity",
                    }
                ],
                selectable: "row",
                change: onChange,
            });

            function onChange() {
                if(this.select) {
                    var selectedItem = $("#gridActivePeople").data("kendoGrid").dataItem(this.select());
                    Person.showPersonDialog(selectedItem.personId,'generalInfoShow');
                }
            }
        }

        function getUserDetails(personId, renderPage) {
            jQuery.ajax({
                url: contextPath + '/person/loadGeneral',
                data: {
                    'personId': personId, 'tab': renderPage
                },
                type: 'POST',
                success: function (data) {
                    $("#personGeneralViewId").html(data)
                    $("#personGeneralViewId").dialog('option', 'width', '420px')
                    $("#personGeneralViewId").dialog('option', 'position', ['center', 'top']);
                    $("#personGeneralViewId").dialog('option', 'modal', 'true');
                    $("#edtBId").parent().remove()
                    $("#personGeneralViewId").dialog('open');

                }
            });

        }


        function loadAll(id) {
            loadRelatedEntities(id)
            loadEventTable(id);
            loadEventNewsTable(id);
            loadTasksTable(id);
            loadAppTable(id);
            loadActivepplTable(id);
        }


        function userPortalByProject(value) {
            jQuery.ajax({
                url: contextPath + '/dashboard/userPortalDetails',
                data: {'project': value},
                type: 'POST',
                success: function (data) {
                    console.log("success");
                    $("#userPortalDiv").html(data);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert("An unexpected error occurred when getting userPortal by project.")
                }
            });
        }

        /**
         * This is being used by the Kendo template to identify we need to show an extra column for the Project Name
         * @returns {boolean}
         */
        function isAllProjectMode() {
            return $("#userProjectId").val() == 0;
        }


        $(document).ready(function () {

            $("#teamMenuId a").css('background-color', '#003366');

            $(".menu-parent-dashboard-user-dashboard").addClass('active');
            $(".menu-parent-dashboard").addClass('active');

            // Avoid dialog to auto open ( if this is all page, why not a global change?)
            $("#personGeneralViewId").dialog({autoOpen: false});

            var myOption = "<option value='0'>All Active</option>";
            <g:if test="${projects.size()>1}">
            $("#userProjectId option:first").before(myOption);
            </g:if>

            // Transform the normal select into a dropdown list, needs to be at last to add default option from above.
            $("#userProjectId").kendoDropDownList({
                change: function(e) {
                    var value = this.value();
                    loadAll(value)
                }
            });
            var projectId = $("#userProjectId").val();

            // Run the process to load all info.
            loadAll(projectId);

        });

    </script>

    <br />
    <br />
    <div class="tdsAssetsApp" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets"></div>
</body>
</html>
