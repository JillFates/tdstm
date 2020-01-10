<tr style="order: 2000">
    <th class="N" nowrap="nowrap">
        Tags
    </th>
    <g:if test="${tagAssetsFromServer}">
        <td nowrap="nowrap" class="N asset-tag-selector-component-content" colspan="7" id="tmAssetTagViewSelector">
            <g:each var="tagList" in="${tagAssetList}" var="tag" status="i">
                <span class="label tag ${tag.css}" style="padding-top: 0px">
                    ${tag.name}
                </span>
            </g:each>
        </td>
    </g:if>
    <g:else>
        <td nowrap="nowrap" class="N asset-tag-selector-component-content" colspan="7" id="tmAssetTagViewSelector">
            <span class="label tag {{tag.css}}" ng-repeat="tag in internalAsset.assetSelector.tag">
                {{tag.name}}
            </span>
        </td>
    </g:else>
</tr>
