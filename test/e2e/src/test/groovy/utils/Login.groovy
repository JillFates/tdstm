package utils
/**
 * This class holds the readCredentials method that will read credentials from a file.
 * In the future this can be escalated to either read a different file or retrieve credentials from DB.
 */
class Login{
    /**
     * Reads the login credentials from a text file.
     * @return String credentials
     */
    String readCredentials() {
        String credentials=''
        File dataFile = new File("C:/TDS/tdstm/test/e2e/src/testDataFile.txt")
        def stream = dataFile.withInputStream {
           credentials=dataFile.text.toString()
        }
    return credentials
    }
}