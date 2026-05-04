package org.neoflex.creditapplicationsupportstarter.interceptor;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferingClientHttpResponse implements ClientHttpResponse {

    private final ClientHttpResponse originalResponse;
    private final byte[] body;

    public BufferingClientHttpResponse(ClientHttpResponse originalResponse, byte[] body) {
        this.originalResponse = originalResponse;
        this.body = body;
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return originalResponse.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return originalResponse.getStatusText();
    }

    @Override
    public void close() {
        originalResponse.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(body);
    }

    @Override
    public org.springframework.http.HttpHeaders getHeaders() {
        return originalResponse.getHeaders();
    }
}
