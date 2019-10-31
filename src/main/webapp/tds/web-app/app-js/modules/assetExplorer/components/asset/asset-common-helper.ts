/**
 * Class to group common functionality used by Show and Create/Edit assets view
 */
export class AssetCommonHelper {
	/**
	 * Determine if the class belongs to Yellow and Green and if the value is set
	 */
	static isHighField(importanceClass: string, value: string): boolean {
		const isImportantClass = 'YG'.indexOf(importanceClass.toUpperCase()) !== -1;
		return isImportantClass && Boolean(value) === false;
	}

	static scrollTo(event: MouseEvent, el: HTMLElement, scrollingEl: HTMLElement) {
		const activeTab = document.getElementsByClassName('btn btn-link nav-link active').item(0);
		activeTab.classList.remove('active');
		const target = event.srcElement as HTMLElement;
		target.classList.add('active');
		scrollingEl.scrollTop = el.offsetTop;
	}
}
