import { Component } from '@angular/core';

@Component({
	selector: 'popup-legends',
	template: `
        <span style="cursor:pointer;" #fieldsLegend (click)="onToggle()"><i class="fa  fa-question"></i></span>
        <kendo-popup [anchor]="fieldsLegend" [popupClass]="'content popup'" *ngIf="show">
            <table class="legendTable field-importance-table">
                <tbody>
                <tr>
                    <th>Color</th>
                    <th>Label Background</th>
                    <th>Value Background</th>
                </tr>
                <tr>
                    <td class="color-name color-required">Yellow</td>
                    <td class="Y">Label</td>
                    <td class="Y">Value</td>
                </tr>
                <tr>
                    <td class="color-name color-required">Green</td>
                    <td class="G">Label</td>
                    <td class="G">Value</td>
                </tr>
                <tr>
                    <td class="color-name">Pink</td>
                    <td class="P">Label</td>
                    <td class="P">Value</td>
                </tr>
                <tr>
                    <td class="color-name">Blue</td>
                    <td class="B">Label</td>
                    <td class="B">Value</td>
                </tr>
                <tr>
                    <td class="color-name">Orange</td>
                    <td class="O">Label</td>
                    <td class="O">Value</td>
                </tr>
                <tr>
                    <td class="color-name">Normal</td>
                    <td class="N">Label</td>
                    <td class="">Value</td>
                </tr>
                <tr>
                    <td class="color-name">Unimportant</td>
                    <td class="U">Label</td>
                    <td class="">Value</td>
                </tr>
                <tr>
                    <td colspan="3" class="required-legend"><span class="color-required">&nbsp;</span>The label text displays in <span class="error">Red Text</span> if the value is blank.</td>
                </tr>
                </tbody>
            </table>
        </kendo-popup>
	`,
	styles: [`
		table { width: 300px;}
    `]
})

export class PopupLegendsComponent {
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}