<%@ page defaultCodec="html" %>
<dependents-component #dependentsComponent
        [dependencies]="dependencies"
        (onAssetShow)="onAssetShowFromDependency($event)"
        (onDependencyShow)="onDependencyShowFromDependency($event)"
>
</dependents-component>

