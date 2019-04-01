<%@ page import="net.transitionmanager.project.Project" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav"/>
    <title>Workflows</title>
    <style>
        /*TODO: REMOVE ON COMPLETE MIGRATION */
        div.content-wrapper {
            background-color: #ecf0f5 !important;
        }
    </style>
</head>

<body>
<tds:subHeader title="Workflows" crumbs="['Admin','Workflows','List']"/>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

        <div class="list" style="margin-left: 10px; margin-right: 10px;">
            <table>
                <thead>
                <tr>
                    <g:sortableColumn property="process" title="Workflow"/>

                    <th class="sortable" style="font-size: 10px;">Used On</th>

                    <g:sortableColumn property="dateCreated" title="Created On"/>

                    <g:sortableColumn property="lastUpdated" title="Updated On"/>

                    <g:sortableColumn property="updatedBy" title="Updated By"/>

                </tr>
                </thead>
                <tbody>
                <g:each in="${workflowInstanceList}" status="i" var="workflows">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="showWorkflowList('${workflows?.id}')">

                        <td nowrap="nowrap">${workflows?.process}</td>
                        <td>${net.transitionmanager.project.Project.findAllByWorkflowCode(workflows?.process)?.name.toString().replace("[", "").replace("]", "")}</td>
                        <td nowrap="nowrap">
                            <tds:convertDateTime date="${workflows?.dateCreated}"/>
                        </td>
                        <td nowrap="nowrap">
                            <tds:convertDateTime date="${workflows?.lastUpdated}"/>
                        </td>

                        <td nowrap="nowrap">${workflows?.updatedBy}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

        <div>
            <g:form action="workflowList" name="workflowForm">
                <input type="hidden" name="workflow" id="workflowId">
            </g:form>
        </div>

    <script type="text/javascript">
        function showWorkflowList(workflowId) {
            $("#workflowId").val(workflowId);
            $("form[name=workflowForm]").submit();
        }
    </script>
    <script>
        currentMenuId = "#adminMenu";

        $('.menu-list-workflows').addClass('active');
        $('.menu-parent-admin').addClass('active');

    </script>
</div>
</body>
</html>
