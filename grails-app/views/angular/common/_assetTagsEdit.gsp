<style>
.asset-tag-selector-component .k-switch.asset-tag {
    top: 0px;
}
</style>
<label>Tags</label>
<tds-asset-tag-selector *ngIf="tagList"
                        [model]="assetTagsModel"
                        [tagList]="tagList"
                        [showSwitch]="false"
                        (valueChange)="onTagValueChange($event)">
</tds-asset-tag-selector>
