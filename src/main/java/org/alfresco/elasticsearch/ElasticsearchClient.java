package org.alfresco.elasticsearch;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * Simple local class to test Elasticsearch clients for different server configurations:
 * - http
 * - http with basic authentication
 * - https with basic authentication
 */
public class ElasticsearchClient
{

    // http, basic, https
    private static final String MODE = "https";

    // "http" settings
    private static final String SERVER_PROTOCOL = "http";
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 9200;
    private static final String BASE_URL = "/";

    // "basic" settings
    private static final String ELASTIC_USER = "elastic";
    private static final String ELASTIC_PASS = "elastic";

    // "https" settings
    private static final String SERVER_PROTOCOL_HTTPS = "https";
    // Use "elastic" user password!
    private static final String ELASTIC_PASS_HTTPS = "yDxX3tb2G2W0biZDDOqM";
    private static final String CA_CRT_PATH = "/Users/aborroy/Desktop/git/es-client/https/certs/ca/ca.crt";

    // Connection pool
    private static final int MAX_TOTAL_CONNECTIONS = 30;
    private static final int MAX_HOST_CONNECTIONS = 30;

    // Timeouts
    private static final int CONNECTION_TIMEOUT = 1000;
    private static final int SOCKET_TIMEOUT = 30000;

    public static void main(String... args) throws Exception
    {

        switch (MODE)
        {
            case "http":
                httpClient();
                break;
            case "basic":
                httpBasicAuthClient();
                break;
            case "https":
                httpsClient();
                break;
            default:
                break;
        }
    }

    private static RequestConfig getRequestConfigBuilder()
    {
        return RequestConfig.custom().setConnectTimeout(CONNECTION_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
    }

    private static void httpClient() throws Exception
    {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(SERVER_NAME, SERVER_PORT, "http"))
                        .setHttpClientConfigCallback(new HttpClientConfigCallback()
                        {
                            @Override
                            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
                            {
                                return httpClientBuilder.setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                                        .setMaxConnPerRoute(MAX_HOST_CONNECTIONS)
                                        .setDefaultRequestConfig(getRequestConfigBuilder());
                            }
                        }).setPathPrefix(BASE_URL));

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(searchResponse.getHits().getTotalHits().value);

    }

    private static void httpBasicAuthClient() throws Exception
    {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ELASTIC_USER, ELASTIC_PASS));

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(SERVER_NAME, SERVER_PORT, SERVER_PROTOCOL))
                        .setHttpClientConfigCallback(new HttpClientConfigCallback()
                        {
                            @Override
                            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
                            {
                                return httpClientBuilder.setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                                        .setMaxConnPerRoute(MAX_HOST_CONNECTIONS)
                                        .setDefaultRequestConfig(getRequestConfigBuilder())
                                        .setDefaultCredentialsProvider(credentialsProvider);
                            }
                        }));

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(searchResponse.getHits().getTotalHits().value);
    }

    private static void httpsClient() throws Exception
    {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(ELASTIC_USER, ELASTIC_PASS_HTTPS));
        Path caCertificatePath = Paths.get(CA_CRT_PATH);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate trustedCa;
        try (InputStream is = Files.newInputStream(caCertificatePath))
        {
            trustedCa = factory.generateCertificate(is);
        }
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);
        SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStore, null);
        final SSLContext sslContext = sslContextBuilder.build();
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(SERVER_NAME, SERVER_PORT, SERVER_PROTOCOL_HTTPS))
                        .setHttpClientConfigCallback(new HttpClientConfigCallback()
                        {
                            @Override
                            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
                            {
                                return httpClientBuilder.setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                                        .setMaxConnPerRoute(MAX_HOST_CONNECTIONS)
                                        .setDefaultRequestConfig(getRequestConfigBuilder())
                                        .setDefaultCredentialsProvider(credentialsProvider)
                                        .setSSLContext(sslContext);
                            }
                        }));
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(searchResponse.getHits().getTotalHits().value);
    }

}
