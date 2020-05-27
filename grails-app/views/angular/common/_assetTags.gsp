<!-- Asset Tags -->
<table class="tdr-detail-list tag-list-view">
    <tr>
        <th class="N">
            <span data-content="${standardFieldSpecs.tagAssets.tip}" data-toggle="popover" data-trigger="hover" data-original-title="" title="">Tags</span>
        </th>
        <td class="fit-tags-to-view">
                <span *ngFor="let tag of assetTags" class="label tag"
                      [ngClass]="tag.css">
                    {{tag.name}}
                </span>
        </td>
    </tr>
</table>

