import grails.test.*

import java.text.SimpleDateFormat

import com.tdssrc.grails.GormUtil
import grails.test.mixin.TestFor
import spock.lang.Specification
import com.tdssrc.grails.TimeUtil

@TestFor(CustomTagLib)
class CustomTagLibTests extends Specification {

    /** Setup metaclass fixtures for mocking. */
    protected void setup() {
    }

    /** Remove metaclass fixtures for mocking. */
    def cleanup() {
    }

    void testConvertDate() {

        // Create a reference test date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        def date = sdf.parse("2012-08-21T01:00:00-0000")

        def correct = true
        def convertedDate
        def sessionTimeZone = ""
        def sessionDateFormat
        def formatsToTest

        // Mock session behaviour
        def session = [
                "getAttribute": { param ->
                    if ("CURR_DT_FORMAT" == param) {
                        return ["CURR_DT_FORMAT": sessionDateFormat]
                    } else if ("CURR_TZ" == param) {
                        return ["CURR_TZ": sessionTimeZone]
                    }
                }
        ]

        CustomTagLib.metaClass.session = session

        // ******************************************
        // Test dates using date format: "MM/DD/YYYY"
        sessionDateFormat = "MM/DD/YYYY"
        formatsToTest = [
                "$TimeUtil.FORMAT_DATE_TIME": [
                        ["GMT": "08/21/2012 01:00 AM"],
                        ["America/Argentina/Buenos_Aires": "08/20/2012 10:00 PM"],
                        ["America/New_York": "08/20/2012 09:00 PM"]
                ],
                "$TimeUtil.FORMAT_DATE"     : [
                        ["GMT": "08/21/2012"],
                        ["America/Argentina/Buenos_Aires": "08/20/2012"],
                        ["America/New_York": "08/20/2012"]
                ]
        ]

        formatsToTest.each { format, formatTestConfigs ->

            formatTestConfigs.each { testConfig ->

                testConfig.each { timeZone, expectedDate ->

                    sessionTimeZone = timeZone

                    convertedDate = applyTemplate('<tds:convertDateTime date="${date}" timeZone="${timeZone}" format="${format}" />', [date: date, timeZone: timeZone, format: format])

                    correct = correct && expectedDate.equals(convertedDate)
                }

            }
        }

        // ******************************************
        // Test dates using date format: "DD/MM/YYYY"
        sessionDateFormat = "DD/MM/YYYY"
        formatsToTest = [
                "$TimeUtil.FORMAT_DATE_TIME": [
                        ["GMT": "21/08/2012 01:00 AM"],
                        ["America/Argentina/Buenos_Aires": "20/08/2012 10:00 PM"],
                        ["America/New_York": "20/08/2012 09:00 PM"]
                ],
                "$TimeUtil.FORMAT_DATE"     : [
                        ["GMT": "21/08/2012"],
                        ["America/Argentina/Buenos_Aires": "20/08/2012"],
                        ["America/New_York": "20/08/2012"]
                ]
        ]

        formatsToTest.each { format, formatTestConfigs ->

            formatTestConfigs.each { testConfig ->

                testConfig.each { timeZone, expectedDate ->

                    sessionTimeZone = timeZone

                    convertedDate = applyTemplate('<tds:convertDateTime date="${date}" timeZone="${timeZone}" format="${format}" />', [date: date, timeZone: timeZone, format: format])

                    correct = correct && expectedDate.equals(convertedDate)
                }

            }
        }
        expect:
        correct
    }

    void testTextAsLink() {

        expect:
        // Just Text
        applyTemplate('<tds:textAsLink text="${text}" />', [text: "p:some more data that is not a URL"]).equals("p:some more data that is not a URL")
        // Testing http
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "http://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing HTTP
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "HTTP://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing https
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "https://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing HTTPS
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "HTTPS://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing ftp
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "ftp://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing FTP
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FTP://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing ftps
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "ftps://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing FTPS
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FTPS://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing smb
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "smb://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing SMB
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "SMB://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing file
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "file://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing FILE
        applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FILE://www.google.com", target: "_blank"]).startsWith("<a href")
        // Testing UNC
        applyTemplate('<tds:textAsLink text="${text}" />', [text: '\\\\hola\\dir\\file']).startsWith('<a href="file://hola/dir/file')
        // A Windows File
        applyTemplate('<tds:textAsLink text="${text}" />', [text: 'p:\\dir\\file name']).startsWith('<a href="file://p%3A%2Fdir%2Ffile+name')
        // Testing Blank Text
        applyTemplate('<tds:textAsLink text="${text}" />', [text: '']).equals('')
        // Testing Null Text
        applyTemplate('<tds:textAsLink text="${text}" />', [text: null]).equals('')
    }

    /*void testSVGIcon() {
        expect:*/
        // Verify it render the svg in a img tag for better support
        //applyTemplate('<tds:svgIcon name="${name}" />', [name: "application"]).startsWith('<img')
        // Test it contacts properly the svg method
        //applyTemplate('<tds:svgIcon name="${name}" />', [name: "application"]).contains('application.svg')
        // Prevent Directory traversal
        //applyTemplate('<tds:svgIcon name="${name}" />', [name: "../application"]).contains('application.svg')
        // Do not fail on empty name
        //applyTemplate('<tds:svgIcon name="${name}" />', [name: ""]).isEmpty()
   /* }*/

}
