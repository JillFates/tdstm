<!-- Asset Tags -->
<tr>
    <th style="padding: 10px 10px 0 0;">
        <label>
            Tags
        </label>
    </th>
    <td style="display: flex;" class="fit-tags-to-view">
        <div *ngFor="let tag of assetTags" class="label tag"
              [ngClass]="tag.css">
            {{tag.name}}
        </div>
    </td>
</tr>
