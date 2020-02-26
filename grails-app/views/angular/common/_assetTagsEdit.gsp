<style>
.asset-tag-selector-component .k-switch.asset-tag {
    top: 0px;
}
</style>
<table class="tdr-detail-list">
    <tr>
        <th class="N">Tags</th>
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
