<!-- Asset Tags -->
<tr>
    <th style="padding: 10px 10px 0 0;">
        <label>
            Tags
        </label>
    </th>
    <td style="display: flex;">
        <div *ngFor="let tag of assetTags" class="badge label tag"
              [ngClass]="tag.css"
              style="width: unset;">
            {{tag.name}}
        </div>
    </td>
</tr>
