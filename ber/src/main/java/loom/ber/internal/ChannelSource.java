package loom.ber.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public final class ChannelSource implements Source
{
	private final ByteBuffer buffer = ByteBuffer.allocate(1);
	
	private final ReadableByteChannel source;

	public ChannelSource (ReadableByteChannel source)
	{
		this.source = source;
	}

	@Override
	public int pull ()
	{
		try
		{
			buffer.clear();
			final int read = source.read(buffer);
			if (read == -1) {
				return -1;
			}
			else {
				buffer.flip();
				return buffer.get();
			}
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
			return source.read(sink);
		}
		catch (IOException e)
		{
			throw new RuntimeException("push", e);
		}
	}
}
