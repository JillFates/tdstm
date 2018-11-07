export class SharedAllAssets {
	/**
	 * Determine if the class belongs to Yellow and Green and if the value is set
	 */
	static isHighField(importanceClass: string, value: string): boolean {
		const isImportantClass = 'YG'.indexOf(importanceClass.toUpperCase()) !== -1;
		return isImportantClass && Boolean(value) === false;
	}
}
