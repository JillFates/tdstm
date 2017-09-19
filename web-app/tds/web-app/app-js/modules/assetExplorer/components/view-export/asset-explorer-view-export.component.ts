import {Component, ViewChild} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {ExcelExportComponent} from '@progress/kendo-angular-excel-export';
import {DomainModel} from '../../../fieldSettings/model/domain.model';
import {FieldImportance} from '../../../fieldSettings/model/field-settings.model';
import {AssetQueryParams} from '../../model/asset-query-params';
import {AssetExplorerService} from '../../service/asset-explorer.service';

@Component({
	selector: 'asset-explorer-view-export',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-export/asset-explorer-view-export.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class AssetExplorerViewExportComponent {
	private columns: any[];
	protected fileName = 'asset_explorer';
	protected dataToExport: any[] = [];
	protected APIExport: any[] = [
		{
			'common.id': 152254,
			'device.model': 'SRW24G4',
			'device.manufacturer': 'LINKSYS'
		},
		{
			'common.id': 152255,
			'device.model': 'ZPHA MODULE',
			'device.manufacturer': 'TippingPoint'
		},
		{
			'common.id': 152256,
			'device.model': 'Slideaway',
			'device.manufacturer': 'ATEN'
		},
		{
			'common.id': 152257,
			'device.model': 'CCM4850',
			'device.manufacturer': 'Avocent'
		},
		{
			'common.id': 152258,
			'device.model': 'CCM4850',
			'device.manufacturer': 'Avocent'
		},
		{
			'common.id': 152259,
			'device.model': 'DSR2035',
			'device.manufacturer': 'Avocent'
		},
		{
			'common.id': 152260,
			'device.model': 'DSR2035',
			'device.manufacturer': 'Avocent'
		},
		{
			'common.id': 152261,
			'device.model': 'DSR8020',
			'device.manufacturer': 'Avocent'
		},
		{
			'common.id': 152262,
			'device.model': 'DSR8020',
			'device.manufacturer': 'Avocent'
		},
		{
			'common.id': 152263,
			'device.model': 'DSR8020',
			'device.manufacturer': 'Avocent'
		},
		{
			'common.id': 152264,
			'device.model': 'Unknown - Firewall',
			'device.manufacturer': 'unknown'
		},
		{
			'common.id': 152265,
			'device.model': 'Unknown - Firewall',
			'device.manufacturer': 'unknown'
		},
		{
			'common.id': 152266,
			'device.model': '2400E',
			'device.manufacturer': 'TippingPoint'
		},
		{
			'common.id': 152267,
			'device.model': '2U Cable Management',
			'device.manufacturer': 'Generic'
		},
		{
			'common.id': 152268,
			'device.model': 'Desktops SxS',
			'device.manufacturer': 'Generic'
		},
		{
			'common.id': 152269,
			'device.model': 'BladeSystem c7000',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152270,
			'device.model': 'BladeSystem c7000',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152271,
			'device.model': 'BladeSystem c7000',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152272,
			'device.model': 'BladeSystem c7000',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152273,
			'device.model': 'BladeSystem c7000',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152274,
			'device.model': 'BladeSystem c7000',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152275,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152276,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152277,
			'device.model': 'ProLiant BL460c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152278,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152279,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152280,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152281,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152282,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152283,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152284,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152285,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152286,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152287,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152288,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152289,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152290,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152291,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152292,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152293,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152294,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152295,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152296,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152297,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152298,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152299,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152300,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152301,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152302,
			'device.model': 'ProLiant BL460c G5',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152303,
			'device.model': 'ProLiant BL460c G5',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152304,
			'device.model': 'ProLiant BL460c G5',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152305,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152306,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152307,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152308,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152309,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152310,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152311,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152312,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152313,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152314,
			'device.model': 'ProLiant BL460c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152315,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152316,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152317,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152318,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152319,
			'device.model': 'ProLiant BL460c G5',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152320,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152321,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152322,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152323,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152324,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152325,
			'device.model': 'ProLiant BL490c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152326,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152327,
			'device.model': 'ProLiant BL460c G5',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152328,
			'device.model': 'ProLiant BL460c G5',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152329,
			'device.model': 'ProLiant BL460c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152330,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152331,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152332,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152333,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152334,
			'device.model': 'ProLiant BL460c G5',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152335,
			'device.model': 'ProLiant BL460c G6',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152336,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152337,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152338,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152339,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152340,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152341,
			'device.model': 'ProLiant BL465c G7',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152342,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152343,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152344,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152345,
			'device.model': 'ProLiant BL460c G1',
			'device.manufacturer': 'HP'
		},
		{
			'common.id': 152346,
			'device.model': 'ProxyAV',
			'device.manufacturer': 'Blue Coat'
		},
		{
			'common.id': 152347,
			'device.model': 'ProxySG',
			'device.manufacturer': 'Blue Coat'
		},
		{
			'common.id': 152348,
			'device.model': null,
			'device.manufacturer': null
		},
		{
			'common.id': 152349,
			'device.model': null,
			'device.manufacturer': null
		},
		{
			'common.id': 152350,
			'device.model': null,
			'device.manufacturer': null
		},
		{
			'common.id': 152351,
			'device.model': null,
			'device.manufacturer': null
		},
		{
			'common.id': 152352,
			'device.model': null,
			'device.manufacturer': null
		},
		{
			'common.id': 152353,
			'device.model': null,
			'device.manufacturer': null
		}
	];

	@ViewChild('excelexport') public excelexport: ExcelExportComponent;

	constructor(public exportParams: AssetQueryParams, private domains: Array<DomainModel>, public activeDialog: UIActiveDialogService, private assetExpService: AssetExplorerService) {

		let configuredColumns = {...this.exportParams.filters.columns};

		this.columns = Object.keys(configuredColumns).map((key) => {
			let definition = {
				name: this.getPropertyColumnField(configuredColumns[key]['domain'], '_', configuredColumns[key]['property']),
				title: configuredColumns[key]['label'],
				width: configuredColumns[key]['width'],
				locked: configuredColumns[key]['locked'],
				cell: {
					background: this.getImportanceColor(configuredColumns[key])
				}
			};

			return definition;
		});

		this.getExportData();
	}

	/**
	 * Get the Color Schema for the Selected Field
	 * @param model
	 * @returns {string}
	 */
	private getImportanceColor(model: any): string {
		let domain = this.domains.find(r => r.domain.toLocaleLowerCase() === model.domain.toLocaleLowerCase());
		let field = domain.fields.find(r => r.label.toLocaleLowerCase() === model.label.toLocaleLowerCase());
		return FieldImportance[field.imp].color;
	}

	/**
	 * Transform the Data information and remove
	 * the dot in the field that cause empty values in Excel
	 */
	private getExportData(): void {
		this.assetExpService.previewQuery(this.exportParams)
			.subscribe(result => {
				this.dataToExport = result['assets'];
			}, err => console.log(err));
	}

	private getPropertyColumnField(domain: string, separator: string, property: string): string {
		let domainDefinition = domain.toLocaleLowerCase();
		return domainDefinition + separator + property;
	}

	protected createFile(): void {
		this.excelexport.save();
		this.activeDialog.close();
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}