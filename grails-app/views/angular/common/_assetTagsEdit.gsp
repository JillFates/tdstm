<style>
.asset-tag-selector-component .k-switch.asset-tag {
    top: 0px;
}
</style>
<table class="tdr-detail-list tag-list-edit">
    <tr>
        <th class="N">
            <span data-content="${standardFieldSpecs.tagAssets.tip}" data-toggle="popover" data-trigger="hover" data-original-title="" title="">Tags</span>
        </th>
        <td>
            <tds-asset-tag-selector *ngIf="tagList"
                                    [model]="assetTagsModel"
                                    [tagList]="tagList"
                                    [showSwitch]="false"
                                    (valueChange)="onTagValueChange($event)">
            </tds-asset-tag-selector>
        </td>
    </tr>
</table>
