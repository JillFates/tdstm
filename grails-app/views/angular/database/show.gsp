<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div class="modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
        <h4 class="modal-title">Database Detail</h4>
    </div>
    <div class="modal-body">
        <div>
            <table style="border: 0">
                <tr>
                    <td colspan="2">

                        <div class="dialog">
                            <g:if test="${errors}">
                                <div id="messageDivId" class="message">${errors}</div>
                            </g:if>
                            <table>
                                <tbody>
                                    <tr class="prop">
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
                                        <td colspan="2" class="valueNW ${standardFieldSpecs.assetName.imp?:''}" style="max-width: 400px; font-weight:bold;">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.assetName}">
                                                ${asset?.assetName}
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
                                        <td colspan="2" style="max-width: 400px;" class="valueNW ${standardFieldSpecs.description.imp?:''}" >
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.description}">
                                                ${asset.description}
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}"/>
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"/>
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
                                    </tr>
                                    <tr class="prop">
                                        <td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
                                            <label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
                                                ${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
                                            </label>
                                        </td>
                                        <td class="valueNW ${standardFieldSpecs.size.imp?:''}">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.size}">
                                                ${asset?.size}&nbsp;${asset.scale?.value()}
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>
                                        <td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.retireDate}">
                                                {{'${asset?.retireDate}' | date:'dd/MM/yy' }} ${  /* TODO  <tds:convertDate>: You cannot use the session in non-request rendering operations */ }
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                        <td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
                                            <label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
                                                ${standardFieldSpecs.moveBundle.label} : Dep. Group
                                            </label>
                                        </td>
                                        <td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}" colspan="3">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
                                                ${asset?.moveBundle} : ${dependencyBundleNumber}
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"/>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset?.maintExpDate}"/>
                                        <td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
                                                {{'${asset?.maintExpDate}' | date:'dd/MM/yy'}} ${  /* TODO  <tds:convertDate>: You cannot use the session in non-request rendering operations */ }
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset?.planStatus}"/>
                                        <td class="valueNW ${standardFieldSpecs.planStatus.imp?:''}" colspan="3">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.planStatus}">
                                                ${asset.planStatus}
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td></td>

                                        <tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}"/>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset?.validation}"/>
                                        <td class="valueNW ${standardFieldSpecs.validation.imp?:''}" colspan="3">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.validation}">
                                                ${asset.validation}
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                    </tr>
                                    <g:render template="/angular/common/customShow" model="[asset:asset, project:project]"></g:render>
                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>
                <tr id="deps">
                    <g:render template="/angular/common/dependentShow" model="[assetEntity:asset]" ></g:render>
                </tr>
                <tr id="commentListId">
                    <g:render template="/angular/common/commentList" model="[asset:asset, prefValue:prefValue, viewUnpublishedValue:viewUnpublishedValue]" ></g:render>
                </tr>
            </table>
        </div>
    </div>
<div class="modal-footer form-group-center">
    <button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
</div>
</div>