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

@Transactional
@Slf4j
class ServiceNowService {
    private static final String DEFAULT_CHARACTER_ENCODING = 'UTF-8'
    private static final String SERVICE_NOW_FILENAME = 'SERVICE_NOW_FILENAME'
    private static final String USERNAME = "Dcorrea"
    private static final String PASSWORD = "boston2004"

    SecurityService securityService

    /**
     * Fetch assets from ServiceNow
     * @param payload
     * @return
     */
    Map fetchAssets(Object payload) {
        log.info('Fetching ServiceNow assets: {}', payload)
        String filename = downloadAndSaveAssetsFile(payload)
        Map result = null
        if (filename) {
            result = ['status': 'success', 'filename': filename]
        } else {
            result = ['status': 'error', 'cause': 'Not able to download requested asset.']
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

            log.info(response.getStatusLine().toString())
            HttpEntity entity = response.getEntity()
            log.info('----------------------------------------')
            log.info(entity.getContentType().toString())
            log.info(response.getFirstHeader("Content-Disposition").getValue())

            InputStream input = null
            OutputStream output = null
            byte[] buffer = new byte[1024]

            try {
                filename = getDestinationFilename(response, payload)
                log.info("Downloading file: {}", filename)
                input = entity.getContent()
                String saveDir = "/tmp/"

                output = new FileOutputStream(saveDir + filename)
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
                    }
                if (input != null)
                    try {
                        input.close()
                    } catch (IOException logOrIgnore) {
                    }
            }
            EntityUtils.consume(entity)
        } catch (Exception e) {
            log.error('Error fetching external resource from ServiceNow.', e)
        } finally {
            response = null
        }

        return filename
    }

    /**
     * Construct the downloaded filename prefix
     * @return
     */
    private String getFilenamePrefix() {
        Date now = new Date()
        MessageDigest.getInstance("MD5").digest(now.toString().bytes).encodeHex().toString()
    }

    /**
     * Construct the destination filename
     * @param response
     * @param payload
     * @return
     */
    private String getDestinationFilename(HttpResponse response, Map payload) {
        String filename = null
        String dispositionValue = response.getFirstHeader("Content-Disposition").getValue()
        int index = dispositionValue.toLowerCase().indexOf("filename=")
        if (index > 0) {
            // example: inline;filename=cmdb_ci_appl.csv
            filename = getFilenamePrefix() + "-" + dispositionValue.substring(index + 9, dispositionValue.length() )
        } else {
            filename = getFilenamePrefix() + "." + payload['format'] == null ? 'csv' : payload['format']
        }
        return filename
    }

}
