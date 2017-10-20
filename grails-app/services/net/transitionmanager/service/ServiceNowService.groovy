package net.transitionmanager.service

import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

import java.security.MessageDigest

@Transactional(readOnly = true)
@Slf4j
class ServiceNowService {
    private static final String DEFAULT_CHARACTER_ENCODING = 'UTF-8'
    private static final String SERVICE_NOW_FILENAME = 'SERVICE_NOW_FILENAME'
    private static final String USERNAME = "Dcorrea"
    private static final String PASSWORD = "boston2004"

    private static final String FILENAME_PREFIX='servicenow-'

    FileSystemService fileSystemService
    SecurityService securityService

    /**
     * Fetch assets from ServiceNow
     * @param payload
     * @return
     */
    Map fetchAssets(Object payload) {
        log.debug 'Fetching ServiceNow assets: {}', payload
        String filename = downloadAndSaveAssetsFile(payload)
        Map result = null
        if (filename) {
            result = [status: 'success', filename: filename]
        } else {
            result = [status: 'error', cause: 'Not able to download requested asset.']
        }
        return result
    }

    /**
     * Post actions executed by callback method
     * @param options
     */
    void notifyFetchResults(Map options) {
        log.info('Got called by callback method with params: {}', options)
        if (options && options['filename']) {
            securityService.session.setAttribute(SERVICE_NOW_FILENAME, options['filename'])
        } else {
            securityService.session.removeAttribute(SERVICE_NOW_FILENAME)
        }
    }

    /**
     * Construct service url from payload
     * @param payload
     * @return
     */
    private String serviceUrl(Map payload) {
        StringBuilder url
        url = new StringBuilder()
        url.append(payload['url'])
        url.append('/')
        url.append(payload['path'])
        url.append('?')
        url.append(payload['format'])
        url.append('&sysparm_query=')
        url.append(URLEncoder.encode(payload['query'], DEFAULT_CHARACTER_ENCODING))
        url.append('&sysparm_fields=')
        url.append(URLEncoder.encode(payload['fieldNames'], DEFAULT_CHARACTER_ENCODING))
        return url.toString()
    }

    /**
     * THIS SHOULD BE DICTATED BY THE CREDENTIAL
     * @return
     */
    private CredentialsProvider getBasicAuth() {
        CredentialsProvider provider = new BasicCredentialsProvider()
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD)
        provider.setCredentials(AuthScope.ANY, credentials)

        return provider
    }

    /**
     * Download file and save it using the file system service
     * @param payload
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private String downloadAndSaveAssetsFile(Map payload) throws ClientProtocolException, IOException {
        HttpResponse response = null
        String filename = null

        try {
            HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(getBasicAuth()).build()
            HttpGet httpGet = new HttpGet(serviceUrl(payload))
            response = httpClient.execute(httpGet)

            log.debug(response.getStatusLine().toString())
            HttpEntity entity = response.getEntity()
            log.debug "----------------------------------------\n{}\n----------------------------------------",
                entity.getContentType().toString()
            log.debug response.getFirstHeader("Content-Disposition").getValue() +

            InputStream input = null
            OutputStream output = null
            byte[] buffer = new byte[1024]

            try {
                String extension = getFilenameExtension(response, payload)
                (filename, output) = fileSystemService.createTemporaryFile(FILENAME_PREFIX, extension)
                input = entity.getContent()
                for (int length; (length = input.read(buffer)) > 0;) {
                    output.write(buffer, 0, length)
                }
                log.info("File successfully downloaded!")
            } finally {
                httpClient.close()

                if (output != null)
                    try {
                        output.close()
                    } catch (IOException logOrIgnore) {
                        log.warn 'Failed to close output stream {}', filename
                    }
                if (input != null)
                    try {
                        input.close()
                    } catch (IOException logOrIgnore) {
                        log.warn 'Failed to close input stream to ServiceNow webservice'
                    }
            }
            EntityUtils.consume(entity)
        } catch (Exception e) {
            log.warn('Error fetching external resource from ServiceNow.', e)
        } finally {
            response = null
        }

        return filename
    }

    /**
     * Determines the proper file extension for the output file based on the response or service definition
     * @param response
     * @param payload
     * @return
     */
    private String getFilenameExtension(HttpResponse response, Map payload) {
        String extension

        // Attempt to strip out the filename extension from the download disposition (example: inline;filename=cmdb_ci_appl.csv)
        String disposition = response.getFirstHeader('Content-Disposition').getValue()
        List parts = disposition.toLowerCase().split('filename=')
        if (parts.size() == 2) {
            parts = parts.split(/\./)
            if (parts.size() == 2) {
                extension = parts[1]
            }
        }

        // Fall back to the payload setting
        if (! extension) {
            extension = (payload.format ? payload.format.toLowerCase() : 'csv')
        }

        return extension
    }
}
