<!-- Asset Tags -->
<tr>
    <th>
        <label>
            Tags
        </label>
    </th>
    <td>
        <span *ngFor="let tag of assetTags" class="badge label tag"
              [ngClass]="tag.css"
              style="">
            {{tag.name}}
        </span>
    </td>
</tr>
