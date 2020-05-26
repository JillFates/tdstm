import {takeUntil} from 'rxjs/operators';
import {pathOr} from 'ramda';

export const TDS_ATTRIBUTE_IS_OPEN = 'tds-attribute-control-is-open';

export class OpenableClosableControlHelper {
	/**
	 * Take an array of controls and setup the listeners to the open/close events
	 * On open/close it adds the attribute to mark the control as 'open' or 'close' accordly
	 * @param controls
	 * @param takeUntilSubject
	 */
	static setUpListeners(controls: {controlType: string, list: any[]}[], takeUntilSubject: any): void {
		const controlTypes = {
			dropDownList: ['wrapper', 'nativeElement'],
			comboList: ['wrapper'],
			tdsComboList: ['innerComboBox', 'wrapper'],
			dateControlList: ['datePicker', 'first', 'element', 'nativeElement'],
			dateTimePartDateControlList: ['datePicker', 'first', 'element', 'nativeElement'],
			dateTimePartTimeControlList: ['timePicker', 'first', 'element', 'nativeElement'],
		};

		const listControls = [];
		controls.forEach((item: {controlType: string, list: any[]}) => {
			item.list.forEach((listItem: any) => {
				const nativeTarget = controlTypes[item.controlType];
				if (item.controlType === 'dateTimePartTimeControlList') {
					const elements = {...listItem, nativeTarget};
					elements.openEvent = elements.openTime;
					elements.closeEvent = elements.closeTime;
					listControls.push(elements);
				} else {
					const elements = {...listItem, nativeTarget};
					elements.openEvent = elements.open;
					elements.closeEvent = elements.close;
					listControls.push(elements);
				}
			});
		});

		listControls.forEach(control => {
			control.openEvent
				.pipe(takeUntil(takeUntilSubject))
				.subscribe(() => OpenableClosableControlHelper.setAttribute(control, 'true'));

			control.closeEvent
				.pipe(takeUntil(takeUntilSubject))
				.subscribe(() => setTimeout(() => OpenableClosableControlHelper.setAttribute(control, 'false'), 200));
		});
	}

	/**
	 * Set the value for the control attribute that indicates if it is open/close
	 * @param control
	 * @param value
	 */
	static setAttribute(control: any, value: string): void {
		const nativeElement = pathOr(null, control.nativeTarget, control);
		if (nativeElement) {
			nativeElement.setAttribute(TDS_ATTRIBUTE_IS_OPEN, value);
		}
	}
}
