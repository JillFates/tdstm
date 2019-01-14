package utils
/**
 * This class holds the readCredentials method that will read credentials from a file.
 * In the future this can be escalated to either read a different file or retrieve credentials from DB.
 */

class Login {
    /**
     * Reads the login credentials from a text file.
     * @return String credentials
     */
    String readCredentials() {
        String credentials=''
        ClassLoader classLoader = getClass().getClassLoader()
        File dataFile = new File(classLoader.getResource("testDataFile.txt").getFile())

        def stream = dataFile.withInputStream {
           credentials=dataFile.text.toString()
        }
        return credentials
    }
}