package com.tdsops.etl

trait RangeChecker {

    /**
     * Validate if a sublist range is valid.
     * @param fromIndex
     * @param toIndex
     * @param size
     */
    def subListRangeCheck (int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw ETLProcessorException.invalidRange('From To initial position must be >= 1')
        if (toIndex > size)
            throw ETLProcessorException.invalidRange('From To range exceeds the overall size of list')
        if (fromIndex > toIndex)
            throw ETLProcessorException.invalidRange('From To range requires that To be >= to From')
    }

    /**
     * Checks if the given index is in range.
     * This method does also check if the index is negative
     * If not, throws an appropriate **ETLProcessorException** exception.
     */
    def rangeCheck (int index, int size) {
        if (index < 0) {
            throw ETLProcessorException.invalidRange('Initial position starts with 1')
        }

        if (index >= size) {
            throw ETLProcessorException.invalidRange('Invalid index = ' + (index + 1))
        }
    }
}
