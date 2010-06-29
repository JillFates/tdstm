// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text-plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// set pagination default (default was 10)
grails.pagination.default="20"
grails.pagination.max="20"

//
// set per-environment serverURL stem for creating absolute links
//
environments {
   development {
      // grails.serverURL = "http://localhost/tdstm"
      grails.serverURL = "http://tmdev.tdsops.com/tdstm"
      log4j {
         appender.'errors.File'="/var/log/tomcat/stacktrace.log"
         appender.logfile = "org.apache.log4j.DailyRollingFileAppender "
         appender.'logfile.File' = "/var/log/tomcat/tdstm.log"
         appender.'logfile.layout' = "org.apache.log4j.PatternLayout"
         appender.'logfile.layout.ConversionPattern' = '%d{[ dd.MM.yy HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n'

         logger {
            grails="debug,stdout,logfile"
            org {
               codehaus.groovy.grails.web.servlet="info,stdout,logfile"
            }
         }
      }
   }
   production {
      grails.serverURL = "http://tm.tdsops.com/tdstm"
      // grails.serverURL = "http://ph1.tdsops.com/tdstm"
      // grails.serverURL = "http://dev01.tdsops.net:8080/tdstm"
      log4j {
         appender.'errors.File'="/var/log/tomcat/stacktrace.log"
         appender.logfile = "org.apache.log4j.DailyRollingFileAppender "
         appender.'logfile.File' = "/var/log/tomcat/tdstm.log"
         appender.'logfile.layout' = "org.apache.log4j.PatternLayout"
         appender.'logfile.layout.ConversionPattern' = '%d{[ dd.MM.yy HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n'

         logger {
            grails="error,stdout,logfile"
            org {
               codehaus.groovy.grails.web.servlet="error,stdout,logfile"
            }
         }
      }
   }
}

// log4j configuration
log4j {
    appender.stdout = "org.apache.log4j.ConsoleAppender"
    appender.'stdout.layout'="org.apache.log4j.PatternLayout"
    appender.'stdout.layout.ConversionPattern'='[%r] %c{2} %m%n'
    appender.stacktraceLog = "org.apache.log4j.FileAppender"
    appender.'stacktraceLog.layout'="org.apache.log4j.PatternLayout"
    appender.'stacktraceLog.layout.ConversionPattern'='[%r] %c{2} %m%n'
    appender.'stacktraceLog.File'="stacktrace.log"
    rootLogger="error,stdout"
    logger {
        grails="error"
        StackTrace="error,stacktraceLog"
        org {
            codehaus.groovy.grails.web.servlet="error"  //  controllers
            codehaus.groovy.grails.web.pages="error" //  GSP
            codehaus.groovy.grails.web.sitemesh="error" //  layouts
            codehaus.groovy.grails."web.mapping.filter"="error" // URL mapping
            codehaus.groovy.grails."web.mapping"="error" // URL mapping
            codehaus.groovy.grails.commons="info" // core / classloading
            codehaus.groovy.grails.plugins="error" // plugins
            codehaus.groovy.grails.orm.hibernate="error" // hibernate integration
            springframework="off"
            hibernate="off"
        }

    }
    additivity.StackTrace=false
}
// For JSecurity
jsecurity.legacy.filter.enabled = true


