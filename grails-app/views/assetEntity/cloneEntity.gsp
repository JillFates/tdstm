<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<form class="form-horizontal" style="min-width: 780px;">
    <div class="form-group" style="height: 50px;">
        <label for="newAssetName" class="col-sm-3 control-label">New asset name: </label>
        <div class="col-sm-6">
            <input type="text" class="form-control" id="newAssetName" placeholder="Change Asset Name" autocomplete="off" required value="${asset.assetEntityInstance.assetName}">
            <label class="lbl-clone-exist" style="font-weight: bold;font-size: 11px;width: 272px;"><span style=" color: red;" class="lbl-error-text">Change name appropriately</span> <span style="cursor: pointer; color: #1c94c4 !important; display: none;" class="open-asset-detail-btn">click here to view </span></label>
            <label class="lbl-clone-name-missing" style="font-weight: bold;font-size: 11px;width: 272px; display: none;"><span style=" color: red;">Asset Name is required</span></label>
        </div>
        <div class="col-sm-3 tooltip-container" style="padding-left: 0px; padding-top: 6px;">
            <i data-toggle="tooltip" data-placement="right" title="Cloned asset will have Environment = 'Select...' and the next available Asset Tag number for device class" class="fa fa-fw fa-question-circle"></i>
        </div>
    </div>
    <tds:hasPermission permission="${Permission.AssetCloneDependencies}">
        <div class="form-group">
            <label for="includeDependencies" class="col-sm-3 control-label">Include Dependencies:</label>
            <div class="col-sm-7">
                <input type="checkbox" id="includeDependencies" style="margin-top: 11px;"> <i data-toggle="tooltip" data-placement="right" title="Clone all existing dependencies as well but will change the status of each to Questioned." class="fa fa-fw fa-question-circle"></i>
            </div>
        </div>
    </tds:hasPermission>

    <div class="modal-footer" style="margin-top: 25px;">
        <div style="float: left;">
            <tds:hasPermission permission="${Permission.AssetCreate}">
                <button type="button" class="btn btn-default clone-action-btn" action="edit"><asset:image src="icons/database_edit.png" border="0px"/> Clone & Edit</button>
                <button type="button" class="btn btn-default clone-action-btn" action="clone"><asset:image src="icons/database_copy.png" border="0px"/> Clone</button>
            </tds:hasPermission>
            <button type="button" class="btn btn-default close-clone-entity"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
        </div>
    </div>
</form>

<div style="display: none;">
    <div class="confirmationDialogOnCloneContent">
        <div class="modal-body">
            <form>
                <div class="box-body">
                    <p >The Asset Name you want to create already exists, do you want to proceed?</p>
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
    div.tooltip-container div.tooltip {
        top: 0px !important;
    }
    div.tooltip-container div.tooltip div.tooltip-arrow {
        top: 24% !important;
    }
</style>
<script>

    currentMenuId = "#assetMenu";
    var title = document.title;
    $("#assetMenuId a").css('background-color','#003366')

    $(document).ready(function() {
        changeDocTitle('${raw(asset.assetEntityInstance.assetName)} Clone');
    });

    var currentAssetId = ${asset.assetEntityInstance.id};
    var currentAssetClass = '${asset.assetEntityInstance.assetClass}'.toUpperCase();
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
            confirmationDialogOnClone.dialog("option", "title", 'Asset already exists');
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

        var assetToClone = {
            assetId: ${asset.assetEntityInstance.id},
            dependencies: includeDependencies,
            name: newAssetName
        };
        EntityCrud.cloneAsset(assetToClone, function(resp){
            document.title = title;
            if(resp && resp.status === 'success' && resp.data.assetId && action == 'edit') {
                EntityCrud.showAssetEditView('${asset.assetEntityInstance.assetClass}'.toUpperCase(), resp.data.assetId);
            } else if(resp && resp.status !== 'success'){
                alert(resp.data)
            }
            $(document).trigger('entityAssetCreated', null);
            $('#cloneEntityView').dialog('close');
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

                var assetToValid =  {
                    assetId: ${asset.assetEntityInstance.id},
                    name: newAssetName
                };

                lastXHRREquest = EntityCrud.isAssetUnique(assetToValid, function(resp){

                    if(resp && resp.status === 'success' && resp.data.unique) {
                        $('.lbl-clone-exist').hide();
                        assetExist = false;
                    } else if(resp && resp.status !== 'success'){
                        $('.lbl-error-text').text(resp.data);
                    } else {
                        $('.lbl-error-text').text('Name already exists,');
                        $('.open-asset-detail-btn').show();
                        currentAssetId =  resp.data.assetId;
                        currentAssetClass = resp.data.assetClass;
                        $('.lbl-clone-exist').show();
                        assetExist = true;
                    }
                });
            }
        }, EntityCrud.getSearchQuietMillis() - 100 );
    });
</script>
