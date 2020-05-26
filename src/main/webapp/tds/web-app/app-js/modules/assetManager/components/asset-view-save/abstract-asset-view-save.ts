import { ViewModel } from '../../../assetExplorer/model/view.model';

/**
 * TODO: identify more identicall repeated code between asset-config and asset-show components and put it here.
 */
export abstract class AbstractAssetViewSave {
	config: any;
	saveOptions: any;
	model: ViewModel;
	protected dataSignature: string;
	private readonly SAVE_BUTTON_ID = 'btnSave';
	private readonly SAVE_AS_BUTTON_ID = 'btnSaveAs';

	getDynamicConfiguration(): any {
		return {
			isDirty: this.isDirty(),
			saveButtonId: this.getSaveButtonId(),
			canSave: this.canSave(),
			canSaveAs: this.canSaveAs(),
			saveButtonLabel: this.getSaveButtonLabel(),
			canShowSaveButton: this.canShowSaveButton(),
			disableSaveButton: this.isSaveButtonDisabled()
		}
	}

	/**
	 * Determines if current model has been changed from the original copy.
	 */
	isDirty(): boolean {
		return this.dataSignature !== this.stringifyCopyOfModel(this.model);
	}

	/**
	 * Removes isFavorite property from view model and returns stringified json.
	 * @param {ViewModel} model
	 * @returns {ViewModel}
	 */
	protected stringifyCopyOfModel(model: ViewModel): string {
		// ignore 'favorite' property.
		let modelCopy = {...model};
		delete modelCopy.isFavorite;
		return JSON.stringify(modelCopy);
	}

	/**
	 * Determines if primary button can be Save or Save all based on permissions.
	 */
	protected getSaveButtonId(): string {
		return this.canSave() ? this.SAVE_BUTTON_ID : this.SAVE_AS_BUTTON_ID;
	}

	/**
	 * Determines if show we can show Save/Save All buttons at all.
	 * @returns {boolean}
	 */
	protected canShowSaveButton(): boolean {
		return this.canSave() || this.canSaveAs();
	}

	/**
	 * Determines if current user can Save view.
	 * @returns {boolean}
	 */
	protected canSave(): boolean {
		return this.saveOptions.save;
	}

	/**
	 * Determine if current user can Save As (new) view.
	 * @returns {boolean}
	 */
	protected canSaveAs(): boolean {
		return (
			this.saveOptions &&
			Array.isArray(this.saveOptions.saveAsOptions) &&
			this.saveOptions.saveAsOptions.length > 0
		);
	}

	/**
	 * Should save button be disabled.
	 */
	abstract isSaveButtonDisabled(): boolean;

	/**
	 * Determines determines the string of the save button label.
	 * @returns {string}
	 */
	protected getSaveButtonLabel() {
		if (!this.canSave() && this.canSaveAs()) {
			return 'GLOBAL.SAVE_AS'
		} else {
			return 'GLOBAL.SAVE'
		}
	}
}
