<!-- Asset Tags -->
<tr>
    <th style="padding: 10px 10px 0 0;">
        <label>
            Tags
        </label>
    </th>
    <td class="fit-tags-to-view">
        <div class="tags-container">
            <span *ngFor="let tag of assetTags" class="label tag"
                  [ngClass]="tag.css">
                {{tag.name}}
            </span>
        </div>
    </td>
</tr>
