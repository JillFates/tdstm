<style>
.asset-tag-selector-component .k-switch.asset-tag {
    top: 0px;
}
</style>
<tr>
    <td class="label N" nowrap="nowrap">
        <label>
            Tags
        </label>
    </td>
    <td nowrap="nowrap" class="N" colspan="4">
        <tds-asset-tag-selector *ngIf="tagList"
                                [model]="assetTagsModel"
                                [tagList]="tagList"
                                (valueChange)="onTagValueChange($event)">
        </tds-asset-tag-selector>
    </td>
</tr>