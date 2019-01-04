import { Component, Inject } from '@angular/core';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import { DependecyService } from '../../service/dependecy.service';

@Component({
	selector: 'asset-dependency',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/asset-dependency/asset-dependency.component.html',
	styles: [`
        table {
            margin-top: 10px;
        }

        .modal-body th {
            background: white !important;
        }

        .modal-body {
            padding-left: 10px;
            padding-right: 10px;
        }

        .modal-body table {
            border-spacing: inherit !important;
        }

        .modal-body table tr th,
        .modal-body table tr td {
            border-top: 0px !important;
        }

        td.lbl-asset-dependency-direction,
        form.dependency-edit-fields td.legend-fields {
            font-weight: bold;
            color: green;
            font: 12px helvetica, arial, sans-serif !important;
        }

        form.dependency-edit-fields table {
            border: 0px;
        }

        form.dependency-edit-fields div.form-group {
            height: 24px;
            font: 12px helvetica, arial, sans-serif !important;
        }

        form.dependency-edit-fields label.control-label {
            text-align: left;
            font-weight: inherit;
        }

        form.dependency-edit-fields label.control-label-title {
            font-weight: bold;
        }

        .btn-default {
            background-color: #f4f4f4;
            color: #444;
            border-color: #ddd;
        }

        .delete-property {
            text-decoration: line-through;
        }

        .half-size {
            width: 60%;
        }

        .separator-table {
            border-top: 2px solid #f4f4f4 !important;
            margin-top: 10px !important;
        }

        .modal-body select,
        .modal-body input.form-control {
            width: 150px;
        }`]
})
export class AssetDependencyComponent extends UIExtraDialog {
	dependencyA: any;
	dependencyB: any;

	constructor(
		@Inject('ASSET_DEP_MODEL') private assetDependency: any,
		private assetService: DependecyService) {
		super('#assetDependency');

		this.dependencyA = assetDependency.assetA && assetDependency.assetA.dependency || null;
		this.dependencyB = assetDependency.assetB && assetDependency.assetB.dependency || null ;

		// while dateCreated and lastUpdated are not coming from server inside of assetDependency.assetX.dependency
		// we need to get them from assetDependency.assetX
		this.dependencyA = this.getDependencyWithDates(this.dependencyA, assetDependency.assetA);
		this.dependencyB = this.getDependencyWithDates(this.dependencyB, assetDependency.assetB);
	}

	private getDependencyWithDates(dependency: any, asset: any): any {
		if (!asset || !dependency) {
			return dependency;
		}

		const { dateCreated, lastUpdated }  = asset;
		return Object.assign({}, dependency, { dateCreated, lastUpdated });
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

}