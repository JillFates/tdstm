<g:if test="${tagAssetsFromServer}">
    <tr>
        <td class="label N" nowrap="nowrap">
            <label>
                Tags
            </label>
        </td>
        <td nowrap="nowrap" class="N asset-tag-selector-component-content" colspan="7" id="tmAssetTagViewSelector">
            <g:each var="tagList" in="${tagAssetList}" var="tag" status="i">
                <span class="label tag ${tag.css}" style="padding-top: 0px">
                    ${tag.name}
                </span>
            </g:each>
        </td>
    </tr>
</g:if>
<g:else>
    <tr>
        <td class="label N" nowrap="nowrap">
            <label>
                Tags
            </label>
        </td>
        <td nowrap="nowrap" class="N asset-tag-selector-component-content" colspan="7" id="tmAssetTagViewSelector">
            <span class="label tag {{tag.css}}" ng-repeat="tag in internalAsset.assetSelector.tag">
                {{tag.name}}
            </span>
        </td>
    </tr>
</g:else>
