<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<form class="form-horizontal" style="min-width: 578px;">
    <div class="form-group" style="height: 50px;">
        <label for="newAssetName" class="col-sm-4 control-label">New asset name: </label>
        <div class="col-sm-6">
            <input type="text" class="form-control" id="newAssetName" placeholder="Change Asset Name" autocomplete="off" required value="${asset.assetEntityInstance.assetName}">
            <label class="lbl-clone-exist" style="font-weight: bold;font-size: 11px;width: 272px;"><span style=" color: red;">Name already exists</span> <span style="float: right; cursor: pointer;" class="open-asset-detail-btn"><span class="glyphicon glyphicon-info-sign"></span> open asset detail</span></label>
            <label class="lbl-clone-name-missing" style="font-weight: bold;font-size: 11px;width: 272px; display: none;"><span style=" color: red;">Asset Name is required</span></label>
        </div>
        <div class="col-sm-1">
            <span class="glyphicon glyphicon-ok icon-clone-ok" style="margin-top: 9px; margin-left: -18px; color: #00a65a; display: none;"></span>
            <span class="glyphicon glyphicon-remove icon-clone-exist" style="margin-top: 9px; margin-left: -18px; color: #dd4b39;"></span>
        </div>
    </div>
    <div class="form-group">
        <label for="includeDependencies" class="col-sm-4 control-label">Include Dependencies:</label>
        <div class="col-sm-7">
            <input type="checkbox" id="includeDependencies" style="margin-top: 11px;"> <i data-toggle="tooltip" data-placement="right" title="Clone all existing dependencies as well but will change the status of each to Questioned." class="fa fa-fw fa-question-circle"></i>
        </div>
    </div>

    <div class="modal-footer" style="margin-top: 25px;">
        <div style="float: left;">
            <button type="button" class="btn btn-default clone-action-btn" action="edit"><img src='${resource(dir:'icons',file:'database_copy.png')}' border='0px'/> Clone & Edit</button>
            <button type="button" class="btn btn-default clone-action-btn" action="clone"><img src='${resource(dir:'icons',file:'database_copy.png')}' border='0px'/> Clone</button>
            <button type="button" class="btn btn-default close-clone-entity"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
        </div>
    </div>
</form>

<div style="display: none;">
    <div class="confirmationDialogOnCloneContent">
        <div class="modal-body">
            <form>
                <div class="box-body">
                    <p >The Asset Name you want to create already exist, do you want to proceed?</p>
                </div><!-- /.box-body -->
            </form>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary pull-left accept-confirmation-btn" ><span class="glyphicon glyphicon-ok"></span> Confirm</button>
            <button type="button" class="btn btn-default pull-right cancel-confirmation-btn" data-dismiss="modal"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
        </div>
    </div>
</div>

<div id="confirmationDialogOnClone" style="display: none;"></div>

<style>
    /* it's going to be removed on refactor */
    .top110 { top: 110px !important; }
</style>
<script>
    currentMenuId = "#assetMenu";
    var title = document.title;
    $("#assetMenuId a").css('background-color','#003366')

    $(document).ready(function() {
        changeDocTitle('${asset.assetEntityInstance.assetName} Clone');
    });

    var currentAssetId = ${asset.assetEntityInstance.id};
    var currentAssetClass = '${asset.assetEntityInstance.assetType}'.toUpperCase();
    var assetExist = true;
    var lastXHRREquest = {};
    $('#confirmationDialogOnClone').dialog({ autoOpen: false });

    $('#cloneEntityView').parent().addClass('top110');

    $("#newAssetName").focusToEnd();

    $(".close-clone-entity").click(function () {
        document.title = title;
        $('#cloneEntityView').dialog('close');
    });

    $('.clone-action-btn').click(function() {
        var action = $(this).attr('action');
        if(assetExist) {
            var confirmationDialogOnClone = $('#confirmationDialogOnClone');
            confirmationDialogOnClone.html($('.confirmationDialogOnCloneContent').html());
            confirmationDialogOnClone.dialog("option", "title", 'Asset already exist');
            confirmationDialogOnClone.dialog('option', 'width', 'auto');
            confirmationDialogOnClone.dialog('option', 'modal', 'true');
            confirmationDialogOnClone.dialog('option', 'position', ['center', 'top']);
            confirmationDialogOnClone.dialog('open');
            $('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
            $('[data-toggle="popover"]').popover();

            $('.accept-confirmation-btn').on('click', function(){
                $('#confirmationDialogOnClone').dialog('close');
                cloneAsset(action);
            });

            $('.cancel-confirmation-btn').on('click', function(){
                $('#confirmationDialogOnClone').dialog('close');
            });
        } else {
            var newAssetName = $('#newAssetName').val();
            var includeDependencies = $('#includeDependencies').prop('checked');
            EntityCrud.cloneAsset(${asset.assetEntityInstance.id}, newAssetName, includeDependencies);
            cloneAsset(action);
        }
    });

    /**
     * Excutes the Clone Action, if edit action mode, the it will promp the edit view.
     * @param action
     */
    function cloneAsset(action) {
        var newAssetName = $('#newAssetName').val();
        var includeDependencies = ($('#includeDependencies').is(":checked")? '1' : '0');
        EntityCrud.cloneAsset(${asset.assetEntityInstance.id}, newAssetName, includeDependencies, function(){
            document.title = title;
            $('#confirmationDialogOnClone').dialog('close');
        });
    }

    $('.open-asset-detail-btn').click(function() {
        document.title = title;
        $('#cloneEntityView').dialog('close');
        EntityCrud.showAssetDetailView(currentAssetClass, currentAssetId);
    });

    $('#newAssetName').keyup(function() {
        tdsCommon.delayEvent(function(){
            if(lastXHRREquest && lastXHRREquest.loadingRequest) {
                lastXHRREquest.abort()
            }
            var newAssetName = $('#newAssetName').val();
            if(!newAssetName || newAssetName.length <= 0 || newAssetName == ''){
                $('.lbl-clone-exist').hide();
                $('.clone-action-btn').prop('disabled', true);
                $('.lbl-clone-name-missing').show();
            } else {
                $('.clone-action-btn').prop('disabled', false);
                $('.lbl-clone-name-missing').hide();
                lastXHRREquest = EntityCrud.isAssetUnique(${asset.assetEntityInstance.id}, newAssetName, function(resp){
                    if(resp && resp.unique) {
                        $('.lbl-clone-exist').hide();
                        $('.icon-clone-exist').hide();
                        $('.icon-clone-ok').show();
                        assetExist = false;
                    } else {
                        currentAssetId =  resp.assetId;
                        currentAssetClass = resp.assetClass;
                        $('.lbl-clone-exist').show();
                        $('.icon-clone-exist').show();
                        $('.icon-clone-ok').hide();
                        assetExist = true;
                    }
                });
            }
        }, EntityCrud.getSearchQuietMillis() - 100 );
    });
</script>
