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
            throw ETLProcessorException.invalidRange('Initial position starts with 1')
        if (toIndex > size)
            throw ETLProcessorException.invalidRange('Invalid to parameter = ' + (toIndex + 1))
        if (fromIndex > toIndex)
            throw ETLProcessorException.invalidRange('Invalid range of parameter, from ' + (fromIndex + 1) + " > to " + (toIndex + 1) + "")
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