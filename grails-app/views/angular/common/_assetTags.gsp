<!-- Asset Tags -->
<tr>
    <td class="label N" nowrap="nowrap">
        <label>
            Tags
        </label>
    </td>
    <td nowrap="nowrap" class="N" colspan="4">
        <span *ngFor="let tag of assetTags" class="label tag"
              [ngClass]="tag.css"
              style="">
            {{tag.name}}
        </span>
    </td>
</tr>
