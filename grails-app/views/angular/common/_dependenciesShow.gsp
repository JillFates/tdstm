<%@ page defaultCodec="html" %>
<dependents-component
        [dependencies]="dependencies"
        (onAssetShow)="onAssetShowFromDependency($event)"
>
</dependents-component>

