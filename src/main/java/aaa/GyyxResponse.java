package aaa;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.Header;

/**
 * Gyyx Response
 */
public class GyyxResponse {
    private Header[] headers;
    private byte[] content;
    private int statusCode;

    GyyxResponse(Header[] headers, byte[] content, int statusCode) {
        super();
        this.headers = headers;
        this.content = content;
        this.statusCode = statusCode;
    }

    /**
     * get special header values
     * 
     * @param name
     *            headerName
     * @return List
     */
    public List<String> getHeaderValues(String name) {
        if (name == null || headers == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(headers).stream()
                .filter(f -> name.equals(f.getName())).map(Header::getValue)
                .collect(Collectors.toList());
    }

    /**
     * get http status code
     * 
     * @return int
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * get http response content as byte[]
     * 
     * @return byte[]
     */
    public byte[] getByteContent() {
        return content;
    }

    /**
     * get http response content as string
     * 
     * @return String
     */
    public String getStringContent() {
        return new String(content);
    }

    /**
     * get http response content as string with Charset
     * 
     * @param charset charset
     * @return
     */
    public String getStringContent(Charset charset) {
        return new String(content, charset);
    }
}
