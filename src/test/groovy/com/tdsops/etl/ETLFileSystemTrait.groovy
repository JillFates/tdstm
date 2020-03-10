package com.tdsops.etl

import com.tdsops.etl.dataset.CSVDataset
import net.transitionmanager.common.FileSystemService

/**
 * This trait implements all the necessary ETL methods that manages file system functionality
 * like create a CSV dataset or delete a file after a test case execution.
 * <p><b>Note:</> It needs always an instance of {@code FileSystemService} to complete any task on It<p>
 */
trait ETLFileSystemTrait {

    /**
     * It deletes a a temporary file in test cases using an instance of filesystem service
     * @param fileName
     * @param fileSystemService an instance of {@code FileSystemService}
     */
    void deleteTemporaryFile(String fileName, FileSystemService fileSystemService) {
        if (fileName) {
            fileSystemService.deleteTemporaryFile(fileName)
        }
    }

    /**
     * Builds a CSV dataSet from a csv content using an instance of filesystem service
     * @param csvContent a CSV String content
     * @param fileSystemService an instance of {@code FileSystemService}
     * @return
     */
    List buildCSVDataSet(String csvContent, FileSystemService fileSystemService) {

        def (String filename, OutputStream dataSetOS) = fileSystemService.createTemporaryFile('unit-test-', 'csv')
        dataSetOS << csvContent.stripIndent().trim()
        dataSetOS.close()

        String fullname = fileSystemService.getTemporaryFullFilename(filename)

        return [fullname, new CSVDataset(fullname)]
    }
}