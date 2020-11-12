package loom.ber.internal;

import java.nio.ByteBuffer;

public final class BufferSource implements Source
{
	private final ByteBuffer source;
	
	public BufferSource (ByteBuffer source)
	{
		this.source = source;
	}

	public int pull ()
	{
		if (source.hasRemaining())
			return source.get();
		else
			return -1;
	}
	
	public int push (ByteBuffer sink)
	{
		if (! source.hasRemaining())
			return -1;
		if (! sink.hasRemaining())
			return 0;
		final int initial = source.position();
		do {
			sink.put(source.get());
		}
		while (source.hasRemaining() && sink.hasRemaining());
		return source.position() - initial;
	}
}