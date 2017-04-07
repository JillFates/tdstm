<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<form class="form-horizontal" style="min-width: 578px;">
    <div class="form-group">
        <label for="newAssetName" class="col-sm-4 control-label">New asset name: </label>
        <div class="col-sm-6">
            <input type="text" class="form-control" id="newAssetName" placeholder="Change Asset Name" required value="${asset.assetEntityInstance.assetName}">
            <label style="font-weight: bold;font-size: 11px;width: 272px;"><span style=" color: red;">Name already exists</span> <span style="float: right;"><span class="glyphicon glyphicon-info-sign"></span> open asset detail</span></label>
        </div>
        <div class="col-sm-1">
            <span class="glyphicon glyphicon-ok" style="margin-top: 9px; margin-left: -18px; color: #00a65a; display: none;"></span>
            <span class="glyphicon glyphicon-remove" style="margin-top: 9px; margin-left: -18px; color: #dd4b39;"></span>
        </div>
    </div>
    <div class="form-group">
        <label for="includeDependencies" class="col-sm-4 control-label">Include Dependencies: </label>
        <div class="col-sm-7">
            <input type="checkbox" id="includeDependencies" style="margin-top: 11px;">
        </div>
    </div>

    <div class="modal-footer" style="margin-top: 25px;">
        <div style="float: left;">
            <button type="button" class="btn btn-default"><img src='${resource(dir:'icons',file:'database_copy.png')}' border='0px'/> Clone & Edit</button>
            <button type="button" class="btn btn-default"><img src='${resource(dir:'icons',file:'database_copy.png')}' border='0px'/> Clone</button>
            <button type="button" class="btn btn-default close-clone-entity"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
        </div>
    </div>
</form>

<style>
    /* it's going to be removed on refactor */
    .top110 { top: 110px !important; }
</style>
<script>
    currentMenuId = "#assetMenu";
    $("#assetMenuId a").css('background-color','#003366')

    $(document).ready(function() {
        changeDocTitle('Clone Asset');
    });

    $('#cloneEntityView').parent().addClass('top110');

    $("#newAssetName").focusToEnd();

    $(".close-clone-entity").click(function () {
        debugger;
        if(EntityCrud.cloneFrom.type == EntityCrud.cloneTypeFrom.VIEW) {
            EntityCrud.showAssetDetailView(EntityCrud.cloneFrom.assetClass, ${asset.assetEntityInstance.id});
        } else if(EntityCrud.cloneFrom.type == EntityCrud.cloneTypeFrom.EDIT) {
            EntityCrud.showAssetEditView(EntityCrud.cloneFrom.assetClass, ${asset.assetEntityInstance.id});
        }

        $('#cloneEntityView').dialog('close');
    });

    $('#newAssetName').keyup(function() {
        tdsCommon.delayEvent(function(){
            alert('Time elapsed!');
        }, EntityCrud.getSearchQuietMillis() );
    });
</script>
