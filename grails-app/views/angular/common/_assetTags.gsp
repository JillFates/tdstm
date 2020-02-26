<!-- Asset Tags -->
<table class="tdr-detail-list tag-list-view">
    <tr>
        <th class="N">Tags</th>
        <td class="fit-tags-to-view">
            <div class="tags-container">
                <span *ngFor="let tag of assetTags" class="label tag"
                      [ngClass]="tag.css">
                    {{tag.name}}
                </span>
            </div>
        </td>
    </tr>
</table>

