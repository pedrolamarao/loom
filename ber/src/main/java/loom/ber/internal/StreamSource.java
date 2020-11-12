package loom.ber.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class StreamSource implements Source
{
	private final InputStream source;

	public StreamSource (InputStream source)
	{
		this.source = source;
	}

	@Override
	public int pull ()
	{
		try
		{
			return source.read();
		} 
		catch (IOException e)
		{
			throw new RuntimeException("pull", e);
		}
	}

	@Override
	public int push (ByteBuffer sink)
	{
		try
		{
			if (sink.hasArray()) {
				final int initial = sink.position();
				final int read = source.read(sink.array(), sink.arrayOffset() + initial, sink.remaining());
				if (read > 0)
					sink.position(initial + read);
				return read;
			}
			else {
				throw new RuntimeException("oops");
			}
		} 
		catch (IOException e)
		{
			throw new RuntimeException("push", e);
		}
	}
}