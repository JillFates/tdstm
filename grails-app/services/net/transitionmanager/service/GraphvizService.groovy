package net.transitionmanager.service

import org.apache.commons.lang3.RandomUtils
import org.codehaus.groovy.grails.commons.GrailsApplication

class GraphvizService {

    GrailsApplication grailsApplication

    // The maximum amount of time given to the dot command
    static final Long DOT_CMD_TIMEOUT_MS = 150000L

    /**
     * Attempts to generate an SVG file from DOT text passed. Upon success SVG creation
     * it deletes both the SVG and DOT files
     * @param filenamePrefix a prefix used when creating the filename that will include the datetime plus a random #
     * @param dotText the DOT syntax used to define the graph
     * @return the SVG file content
     * @throws RuntimeException when the generation fails, the exception message will contain the output from the dot command
     */
    String generateSVGFromDOT(String filenamePrefix, String dotText) {
        def conf = grailsApplication.config.graph
        String tmpDir = conf.tmpDir
        String graphType = conf?.graphviz?.graphType

        File graphFile = touchFile(tmpDir, filenamePrefix, graphType)
        File dotFile = createDotFile(tmpDir, filenamePrefix, dotText)

        def dotExec = conf?.graphviz?.dotCmd
        def sout = new StringBuilder()
        def serr = new StringBuilder()
        String cmd = "$dotExec -T${graphType} -v -o ${graphFile} ${dotFile}"
        log.debug "generateSVGFromDOT: about to execute command: $cmd"

        def proc = cmd.execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(DOT_CMD_TIMEOUT_MS)

        log.debug "generateSVGFromDOT: process stdout=$sout"
        log.debug "generateSVGFromDOT: process stderr=$serr"

        if (proc.exitValue() != 0) {
            File errFile = touchFile(tmpDir, filenamePrefix, "err")
            errFile << "exit code:\n\n${proc.exitValue()}\n\nstderr:\n$serr\n\nstdout:\n$sout"
            throw new RuntimeException("Exit code: ${proc.exitValue()}\n stderr: $serr\n stdout: $sout")
        }

        String svgText = graphFile.text
        deleteFile(graphFile)
        deleteFile(dotFile)
        return svgText
    }

    /**
     * Create a DOT file with dotText as content in the tmpDir passed by parameter
     * @param tmpDir temporary filesystem folder
     * @param filenamePrefix filename prefix
     * @param dotText the DOT syntax used to define the graph
     * @return new created DOT file
     */
    private File createDotFile(String tmpDir, String filenamePrefix, String dotText) {
        File dotFile = touchFile(tmpDir, filenamePrefix, "dot")
        dotFile.write dotText
        return dotFile
    }

    /**
     * Creates a new file in the tmpDir passed by parameter
     * @param tmpDir temporary filesystem folder
     * @param filenamePrefix filename prefix
     * @param fileExtension file extension
     * @return a new created file
     */
    private File touchFile(String tmpDir, String filenamePrefix, String fileExtension) {
        int random = RandomUtils.nextInt()
        String filename = filenamePrefix + '-' + new Date().format('yyyyMMdd-HHmmss') + '-' + random + '.' + fileExtension
        return new File(tmpDir, filename)
    }

    /**
     * Deletes a file
     * @param file the file to delete
     */
    private void deleteFile(File file) {
        try {
            if (file.delete()) {
                log.debug file.absolutePath + " deleted successfully"
            }
        } catch (e) {
            log.error "Failed to delete ${file.absolutePath} : ${e.message}"
        }
    }
}
