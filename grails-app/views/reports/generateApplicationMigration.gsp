<div class="body" style="width:100%" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
    <div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
        <b>${project.name} : ${moveBundle}</b><br/>
        This analysis was performed on <tds:convertDateTime date="${new Date()}" format="12hrs" /> for ${tds.currentPersonName()}.
    </div>
    <div style="color: black; font-size: 15px;text-align: center;">
        ${time}
    </div>
    ${eventErrorString}
    <br/>
    <table class="event-day-table" ng-controller="tds.comments.controller.MainController as comments">
        <thead>
        <tr>
            <th>Name</th>
            <th>SME</th>
            <th>Start</th>
            <th>Test</th>
            <th>Finish</th>
            <th>Duration (hh:mm)</th>
            <th>Window</th>
        </tr>
        </thead>
        <tbody>
        <g:each var="appList" in="${appList}" var="application" status="i">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" align="center">
                <td>
                    <a data-asset-class="${application.app.assetClass}" data-asset-id="${application.app.id}"
                       href="javascript: void(0);"
                       class="inlineLink">${application.app.assetName}</a>
                </td>
                <td>${application.app.sme}</td>
                <td>${application.startTime}</td>
                <td>${application.workflow }</td>
                <td>${application.finishTime}</td>
                <td>${application.duration}</td>
                <td>
                    <g:if test="${application.customParam}">
                        <span style="color:${application.windowColor};" > ${application.customParam}</span>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <g:render template="/assetEntity/modelDialog"/>
    <g:render template="/assetEntity/entityCrudDivs" />
    <g:render template="/assetEntity/dependentAdd" />
    <g:render template="/assetEntity/initAssetEntityData"/>
</div>
