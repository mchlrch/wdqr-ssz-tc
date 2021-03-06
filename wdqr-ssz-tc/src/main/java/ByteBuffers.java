import java.nio.ByteBuffer;
import java.nio.charset.Charset;

//import static org.bitbucket.cowwoc.requirements.core.Requirements.requireThat;

/**
 * ByteBuffer helper functions.
 *
 * @author Gili Tzabari
 */
public final class ByteBuffers
{
    /**
     * @param buffer  a {@Code ByteBuffer}
     * @param charset the character set of the bytes
     * @return the {@code String} representation of the bytes
     */
    public static String toString(ByteBuffer buffer, Charset charset)
    {
//        requireThat("buffer", buffer).isNotNull();
    	if (buffer == null) throw new RuntimeException("buffer == null");
    	
        byte[] bytes;
        if (buffer.hasArray())
            bytes = buffer.array();
        else
        {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes, 0, bytes.length);
        }
        return new String(bytes, charset);
    }

    /**
     * Prevent construction.
     */
    private ByteBuffers()
    {
    }
}