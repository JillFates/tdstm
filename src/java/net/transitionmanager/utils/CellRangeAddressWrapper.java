package net.transitionmanager.utils;

import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Wrapper that helps to compare CellRangeAddress to other in cache (dictionary) structures
 * Created by octavio on 6/21/16.
 */
class CellRangeAddressWrapper implements Comparable<CellRangeAddressWrapper>{
	public CellRangeAddress range;

	/**
	 * @param theRange the CellRangeAddress object to wrap.
	 */
	public CellRangeAddressWrapper(CellRangeAddress theRange) {
		this.range = theRange;
	}

	/**
	 * @param o the object to compare.
	 * @return -1 the current instance is prior to the object in parameter, 0: equal, 1: after...
	 */
	public int compareTo(CellRangeAddressWrapper o) {

		if (range.getFirstColumn() < o.range.getFirstColumn()
				&& range.getFirstRow() < o.range.getFirstRow()) {
			return -1;
		} else if (range.getFirstColumn() == o.range.getFirstColumn()
				&& range.getFirstRow() == o.range.getFirstRow()) {
			return 0;
		} else {
			return 1;
		}

	}
}
