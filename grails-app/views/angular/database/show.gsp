<!-- database-show.component.ts -->
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span aria-hidden="true">×</span></button>
        <h4 class="modal-title">Database Detail</h4>
    </div>
    <div class="modal-body">
        <div>
            <table>
                <tr>
                    <td colspan="2" class="dialog-container">
                        <div class="dialog">
                            <g:if test="${errors}">
                                <div id="messageDivId" class="message">${errors}</div>
                            </g:if>
                            <table>
                                <tbody>
                                    <tr class="prop">
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
                                        <td colspan="3" class="valueNW ${standardFieldSpecs.assetName.imp?:''}" style="max-width: 400px; font-weight:bold;">${asset?.assetName}</td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
                                        <td colspan="3" style="max-width: 400px;" class="valueNW ${standardFieldSpecs.description.imp?:''}" >${asset.description}</td>
                                    </tr>
                                    <tr class="prop">
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}"/>
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"/>
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
                                    </tr>
                                    <tr class="prop">
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.size}" value="${asset?.size}"/>
                                        <td class="valueNW ${standardFieldSpecs.size.imp?:''}">${asset?.size}&nbsp;${asset.scale?.value()}</td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>
                                        <td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}"><tds:convertDate date="${asset?.retireDate}" endian = "${dateFormat}" /></td>
                                        <td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
                                            <label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
                                                ${standardFieldSpecs.moveBundle.label} : Dep. Group
                                            </label>
                                        </td>
                                        <td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">${asset?.moveBundle}
                                            <tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"/>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset?.maintExpDate}"/>
                                        <td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}"><tds:convertDate date="${asset?.maintExpDate}" endian = "${dateFormat}" /></td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset?.planStatus}"/>
                                        <td class="valueNW ${standardFieldSpecs.planStatus.imp?:''}" colspan="3">${asset.planStatus}</td>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}"/>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset?.validation}"/>
                                        <td class="valueNW ${standardFieldSpecs.validation.imp?:''}" colspan="3">${asset.validation}</td>
                                    </tr>
                                    <g:render template="/angular/common/customShow" model="[asset:asset, project:project]"></g:render>
                                    <g:render template="/angular/common/assetTags"></g:render>
                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>

                <tr>
                    <td colspan="2" class="dates-info-container">
                        <table class="dates-info">
                            <tr>
                                <td class="date-created">Date created: ${dateCreated}</td>
                                <td class="last-updated">Last updated: ${lastUpdated}</td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <tr id="deps">
                    <g:render template="/angular/common/dependentShow" model="[assetEntity:asset]" ></g:render>
                </tr>
                <tr id="commentListId">
                    <g:render template="/angular/common/commentList" model="[asset:asset, prefValue:prefValue, viewUnpublishedValue:viewUnpublishedValue, currentUserId: currentUserId]" ></g:render>
                </tr>
            </table>
        </div>
    </div>
    <div class="modal-footer form-group-center">
        <button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
        <tds:hasPermission permission="${Permission.AssetDelete}">
            <button class="btn btn-danger" (click)="onDeleteAsset()" type="button"><span class="glyphicon glyphicon-trash"></span> Delete</button>
        </tds:hasPermission>
        <button class="btn btn-primary pull-left" (click)="showAssetEditView()" type="button"><span class="glyphicon glyphicon-pencil"></span> Edit</button>
        <button class="btn btn-default pull-left" (click)="onCloneAsset()" type="button"><span class="glyphicon glyphicon-duplicate"></span> Clone </button>
        <a [href]="getGraphUrl()" class="btn btn-default pull-left"><i class="fa fa-fw fa-sitemap"></i> Arch Graph </a>
    </div>
</div>