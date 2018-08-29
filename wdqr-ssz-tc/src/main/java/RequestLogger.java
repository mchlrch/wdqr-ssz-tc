import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

//import static org.bitbucket.cowwoc.requirements.core.Requirements.requireThat;

/**
 * Logs HttpClient request/response traffic.
 *
 * @author Gili Tzabari
 */
public final class RequestLogger
{
    /**
     * {@code true} if request values should be grouped together, and response values should be grouped together to improve readability.
     * {@code false} if events should be logged as soon as they arrive (useful when diagnosing performance problems).
     */
    private static final boolean IMPROVED_READABILITY = true;
    private final AtomicLong nextId = new AtomicLong();
    private final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    /**
     * Attaches event listeners to a request.
     *
     * @param request the request to listen to
     * @return the request
     * @throws NullPointerException if {@code request} is null
     */
    public Request listenTo(Request request)
    {
//    	requireThat("request", request).isNotNull();
        if (request == null) throw new RuntimeException("request == null");
        
        if (IMPROVED_READABILITY)
            improvedReadability(request);
        else
            correctTiming(request);
        return request;
    }

    /**
     * Group events for improved readability.
     *
     * @param request the request to listen to
     */
    private void improvedReadability(Request request)
    {
        long id = nextId.getAndIncrement();
        StringBuilder group = new StringBuilder();
        request.onRequestBegin(theRequest -> group.append("Request " + id + "\n" +
            id + " > " + theRequest.getMethod() + " " + theRequest.getURI() + "\n"));
        request.onRequestHeaders(theRequest ->
        {
            for (HttpField header : theRequest.getHeaders())
                group.append(id + " > " + header + "\n");
        });

        StringBuilder contentBuffer = new StringBuilder();
        request.onRequestContent((theRequest, content) ->
            contentBuffer.append(ByteBuffers.toString(content, getCharset(theRequest.getHeaders()))));
        request.onRequestSuccess(theRequest ->
        {
            if (contentBuffer.length() > 0)
            {
                group.append("\n" +
                    contentBuffer.toString());
            }
            log.debug(group.toString());
            contentBuffer.delete(0, contentBuffer.length());
            group.delete(0, group.length());
        });

        request.onResponseBegin(theResponse ->
        {
            group.append("Response " + id + "\n" +
                id + " < " + theResponse.getVersion() + " " + theResponse.getStatus());
            if (theResponse.getReason() != null)
                group.append(" " + theResponse.getReason());
            group.append("\n");
        });
        request.onResponseHeaders(theResponse ->
        {
            for (HttpField header : theResponse.getHeaders())
                group.append(id + " < " + header + "\n");
        });
        request.onResponseContent((theResponse, content) ->
            contentBuffer.append(ByteBuffers.toString(content, getCharset(theResponse.getHeaders()))));
        request.onResponseSuccess(theResponse ->
        {
            if (contentBuffer.length() > 0)
            {
                group.append("\n" +
                    contentBuffer.toString());
            }
            log.debug(group.toString());
        });
    }

    /**
     * Log events as they come in.
     *
     * @param request the request to listen to
     */
    private void correctTiming(Request request)
    {
        long id = nextId.getAndIncrement();
        request.onRequestBegin(theRequest -> log.debug(id + " > " + theRequest.getMethod() + " " + theRequest.getURI()));
        request.onRequestHeaders(theRequest ->
        {
            for (HttpField header : theRequest.getHeaders())
                log.debug(id + " > " + header);
        });
        request.onRequestContent((theRequest, content) -> log.debug(id + " >> " +
            ByteBuffers.toString(content, getCharset(theRequest.getHeaders()))));

        request.onResponseBegin(theResponse ->
        {
            StringBuilder line = new StringBuilder(id + " < " + theResponse.getVersion() + " " + theResponse.getStatus());
            if (theResponse.getReason() != null)
                line.append(" " + theResponse.getReason());
            log.debug(line.toString());
        });
        request.onResponseHeaders(theResponse ->
        {
            for (HttpField header : theResponse.getHeaders())
                log.debug(id + " < " + header);
        });
        StringBuilder responseBody = new StringBuilder();
        request.onResponseContent((theResponse, content) ->
            responseBody.append(ByteBuffers.toString(content, getCharset(theResponse.getHeaders()))));
        request.onResponseSuccess(theResponse -> log.debug(id + " << " + responseBody));
    }

    /**
     * @param headers HTTP headers
     * @return the charset associated with the request or response body
     */
    private Charset getCharset(HttpFields headers)
    {
        String contentType = headers.get(HttpHeader.CONTENT_TYPE);
        if (contentType == null)
            return StandardCharsets.UTF_8;
        String[] tokens = contentType.toLowerCase(Locale.US).split("charset=");
        if (tokens.length != 2)
            return StandardCharsets.UTF_8;
        // Remove semicolons or quotes
        String encoding = tokens[1].replaceAll("[;\"]", "");
        return Charset.forName(encoding);
    }
}