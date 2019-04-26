<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page import="net.transitionmanager.asset.AssetCableMap" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav"/>
    <link type="text/css" rel="stylesheet" href="${assetPath(src: 'css/rackLayout.css')}"/>
    <link type="text/css" rel="stylesheet" href="${assetPath(src: 'css/jquery.autocomplete.css')}"/>
    <link type="text/css" rel="stylesheet" href="${assetPath(src: 'css/ui.datepicker.css')}"/>

    <g:javascript src="asset.tranman.js"/>
    <g:javascript src="room.rack.combined.js"/>
    <g:javascript src="entity.crud.js"/>
    <g:javascript src="model.manufacturer.js"/>
    <g:render template="/layouts/responsiveAngularResources"/>
    <asset:javascript src="select2.js"/>

    <title>Rack View</title>

</head>

<body>
<tds:subHeader title="Rack Elevations" crumbs="['Data Center', 'Rack Elevations']"/>
<div class="body" style="width:98%; min-width:1385px" ng-app="tdsComments"
     ng-controller="tds.comments.controller.MainController as comments">

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="dialog">
        <g:form action="generateElevations" name="rackLayoutCreate" method="post" target="_blank"
                onsubmit="return submitForm(this);" style="border: 1px solid black; width: 100%; display: flex; align-items: baseline;  position:relative; flex-direction: column; flex-flow: wrap;">
            <input type="hidden" id="redirectTo" value="rack"/>
            <input type="hidden" id="fromRoomOrRack" value="rack"/>
            <table style="width:auto; border: none; float: left">
                <tbody>
                <tr>
                    <td>
                        <label><b>Bundle</b></label><br/>
                        <select id="bundleId" name="moveBundle" multiple="multiple" size="3"
                                onchange="getRackDetails(this.id)" style="width:300px; height:96px">
                            <option value="all" selected="selected">All</option>
                            <g:each in="${moveBundleList}" var="moveBundle">
                                <option value="${moveBundle?.id}">${moveBundle?.name}</option>
                            </g:each>
                        </select>
                    </td>

                    <td>
                        <label><b>Source</b></label><br/>
                        <select id="sourceRackIdSelect" multiple="multiple" name="sourcerack" style="width:400px; height:96px"
                                size="4">
                            <option value="null" selected="selected">All</option>
                        </select>
                    </td>

                    <td>
                        <div style="width:450px">
                            <label><b>Target</b></label><br/>
                            <select id="targetRackIdSelect" multiple="multiple" name="targetrack" style="width:400px; height:96px"
                                    size="4">
                                <option value="null" selected="selected">All</option>
                            </select>
                        </div>
                    </td>

                    <td>
                        <div style="width:200px">
                            <label for="frontView"><input type="checkbox" name="frontView"
                                                          id="frontView" ${frontCheck ? 'checked="checked"' : ''}/>&nbsp;Front View
                            </label><br/>
                            <label for="backViewId"><input type="checkbox" name="backView"
                                                           id="backViewId" ${backCheck ? 'checked="checked"' : ''}/>&nbsp;Back View (power/cabling)
                            </label><br/>
                            <label for="bundleNameId"><input type="checkbox" name="bundleName"
                                                             id="bundleNameId" ${wBundleCheck ? 'checked="checked"' : ''}/>&nbsp;Render bundle names
                            </label><br/>
                            <%-- <label for="showCabling"><input type="checkbox" name="showCabling" id="showCabling" ${wDCheck ? 'checked="checked"' :'' }/>&nbsp;Render diagrams</label><br /> --%>
                            <label for="otherBundleId"><input type="checkbox" name="otherBundle"
                                                              id="otherBundleId" ${woBundleCheck ? 'checked="checked"' : ''}/>&nbsp;Include assets from other bundles
                            </label><br/>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
            <div class="action-wrapper">
                <input type="hidden" id="viewMode" name="viewMode" value=""/>
                <button type="submit" class="btn btn-default submit" value="Generate" id="generateId"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Generate</button>
                <button type="submit" class="btn btn-default submit" value="Print View"><span class="glyphicon glyphicon-print" aria-hidden="true"></span> Print View</button>
            </div>
        </g:form>
    </div>

    <div style="display: none;" id="cablingDialogId"></div>

    <div id="racksLayout" style="width:100%; overflow-x:auto; border: 1px solid black">

        <h3 style="margin: 10px;">Enter your selection criteria and options then press Generate or Print View to view the rack elevations</h3>

    </div>

    <div id="listDialog" title="Asset List" style="display: none;">
        <div class="dialog">
            <table id="listDiv">
            </table>
        </div>
    </div>

    <g:render template="/assetEntity/modelDialog"/>
    <g:render template="/assetEntity/entityCrudDivs"/>
    <g:render template="/assetEntity/dependentAdd"/>
    <g:render template="/assetEntity/initAssetEntityData"/>

    <%-- TODO : JPM 10/2014 : This hidden role input field is NOT in a form so it is bogus --%>
    <input type="hidden" id="role" value="role"/>

    <script type="text/javascript">

        function updateRackDetails(e) {
            var rackDetails = eval('(' + e.responseText + ')')
            var sourceSelectObj = $('#sourceRackIdSelect');
            var targetSelectObj = $('#targetRackIdSelect');
            var sourceRacks = rackDetails[0].sourceRackList;
            var targetRacks = rackDetails[0].targetRackList;
            generateOptions(sourceSelectObj, sourceRacks, 'none');
            generateOptions(targetSelectObj, targetRacks, 'all');

            var targetList = "${targetRackFilter}";
            var targetArray = targetList.split(",");
            if (sourceList == 'none') {
                $("#sourceRackIdSelect option[value='none']").attr('selected', true);
            } else if (targetArray.length > 1 || targetList != "") {
                for (i = 0; i < targetArray.length; i++) {
                    var optvalue = targetArray[i].trim();
                    $("#targetRackIdSelect option[value=" + optvalue + "]").attr('selected', 'selected');
                    $("#targetRackIdSelect option[value='']").attr('selected', false);
                }
            } else {
                $("#targetRackIdSelect option[value='']").attr('selected', 'selected');
            }

            var sourceList = "${sourceRackFilter}"

            var sourceArray = sourceList.split(",")
            if (sourceList == 'none') {
                $("#sourceRackIdSelect option[value='none']").attr('selected', true);
            } else if (sourceArray.length >= 1 && sourceList != "") {
                for (i = 0; i < sourceArray.length; i++) {
                    var optsourcevalue = sourceArray[i].trim();
                    $("#sourceRackIdSelect option[value=" + optsourcevalue + "]").attr('selected', 'selected');
                    $("#sourceRackIdSelect option[value='']").attr('selected', false);
                    $("#sourceRackIdSelect option[value='none']").attr('selected', false);
                }
            } else {
                $("#sourceRackIdSelect option[value='']").attr('selected', 'selected');
                $("#sourceRackIdSelect option[value='none']").attr('selected', false);
            }
            /* Start with generated default */
            // $('input[value=Generate]').click();
        }
        function generateOptions(selectObj, racks, sel) {
            if (racks) {
                var length = racks.length
                if (sel == 'none')
                    selectObj.html("<option value=''>All</option><option value='none' selected='selected'>None</option>");
                else
                    selectObj.html("<option value='' selected='selected'>All</option><option value='none'>None</option>");

                racks.map(function (e) {
                    var locvalue = e.location ? e.location : 'blank';
                    var rmvalue = e.room ? e.room : 'blank';
                    var ravalue = e.tag ? e.tag : 'blank';
                    return ({'value': e.id, 'innerHTML': locvalue + "/" + rmvalue + "/" + ravalue});
                }).sort(function (a, b) {
                    var compA = a.innerHTML;
                    var compB = b.innerHTML;
                    return (compA < compB) ? -1 : (compA > compB) ? 1 : 0;
                }).each(function (e) {
                    var option = document.createElement("option");
                    option.value = e.value;
                    option.innerHTML = e.innerHTML;
                    selectObj.append(option);
                });
            }
        }

        // Global variable that is used to hold onto async requests so that if the user changes the criteria
        // and resubmits before the request finishes, that it can be aborted.
        var reqLoadRack;

        function submitForm(form) {
            if ($("#bundleId").val() == 'null') {
                alert("Please select bundle");
                return false;
            }

            if (!$("#frontView").is(":checked") && !$("#backViewId").is(":checked")) {
                alert("Please select print view");
                return false;
            }

            var viewMode = $('#viewMode').val();

            if (viewMode == 'Generate') {
                <%-- calling this dialog close for some odd reason causes a popup window to appear with rack elevation --%>
                // $("#cablingDialogId").dialog("close")
                $('#racksLayout').html('Loading...');

                if (reqLoadRack) {
                    reqLoadRack.abort();
                }

                reqLoadRack = jQuery.ajax({
                    url: $(form).attr('action'),
                    data: $(form).serialize(),
                    type: 'POST',
                    success: function (data) {
                        getAssignedDetails('rack', '');
                        $('#racksLayout').html(data);
                    }
                });
                return false;
            } else if (viewMode = "Print View") {
                return true;
            }
            console.log("submitForm() didn't handle viewMode '" + viewMode + "'");
            return false;
        }

        $(document).ready(function () {
            $("#editDialog").dialog({autoOpen: false})
            $("#createDialog").dialog({autoOpen: false})
            $("#listDialog").dialog({autoOpen: false})
            $("#showAssetList").dialog({autoOpen: false})
        })
        // Script to get the combined rack list
        function getRackDetails(objId) {
            var bundles = new Array()
            $("#" + objId + " option:selected").each(function () {
                bundles.push($(this).val())
            });

            ${remoteFunction(action:'retrieveRackDetails', params:'\'bundles=\' +bundles', onComplete:'updateRackDetails(XMLHttpRequest)')}
        }

        // Load the Source and Target Racks with possible options based on the currently selected Bundle
        (function ($) {
            var bundleObj = $("#bundleId");
            var bundle = "${bundle}"
            var bundleArray = bundle.split(",")
            <%-- TODO : JPM 10/2014 : watched this in debugger and saw that this will never work. Do not understand the purpose of this either --%>
            if (bundleArray != null && bundleArray != '' && bundleArray != 'all' && bundleArray.size() > 0) {
                for (i = 0; i < bundleArray.size(); i++) {
                    var optvalue = bundleArray[i].trim();
                    $("#bundleId option[value=" + optvalue + "]").attr('selected', 'selected');
                    $("#bundleId option[value=all]").attr('selected', false);
                }
            } else {
                var isCurrentBundle = '${isCurrentBundle}'
                $("#bundleId option[value='all']").attr('selected', true);
                if (isCurrentBundle == "true") {
                    bundleObj.val('${currentBundle}');
                }
            }
            var bundleId = bundleObj.val();
            ${remoteFunction(action:'retrieveRackDetails', params:'\'bundles=\' + bundleId', onComplete:'updateRackDetails(XMLHttpRequest)')};

            $('button.submit').click(function () {
                $('#viewMode').val($(this).val());
            });
        })(jQuery);

        function createAssetPage(type, source, rack, roomName, location, position) {
            ${remoteFunction(action:'create',controller:'assetEntity',params:['redirectTo':'rack'], onComplete:'createEntityView(XMLHttpRequest,type,source,rack,roomName,location,position)')}
        }

        function createBladeDialog(source, blade, position, manufacturer, assetType, assetEntityId, moveBundleId) {
            var redirectTo = 'rack'
            new Ajax.Request('/assetEntity/create?redirectTo=' + redirectTo + '&assetType=' + assetType + '&manufacturer=' + manufacturer, {
                asynchronous: true, evalScripts: true,
                onSuccess: function (e) {
                    if (e.responseText.substr(0, 1) == '{') {
                        var resp = eval('(' + e.responseText + ')');
                        alert(resp.errMsg)
                    } else {
                        createEntityView(e, 'Server');
                        updateAssetBladeInfo(source, blade, position, manufacturer, moveBundleId);
                    }
                },
                onFailure: function (jqXHR, textStatus, errorThrown) {
                    alert("An unexpected error occurred. Please close and reload form to see if the problem persists")
                }
            })
        }

        currentMenuId = "#racksMenu";
        $(".menu-parent-data-centers-rack-elevation").addClass('active');
        $(".menu-parent-data-centers").addClass('active');
    </script>
</div>
</body>
</html>
