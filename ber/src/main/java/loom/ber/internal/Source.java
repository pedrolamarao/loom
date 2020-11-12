package loom.ber.internal;

import java.nio.ByteBuffer;

@SuppressWarnings("preview")
public sealed interface Source 
	permits BufferSource, StreamSource, ChannelSource
{
	int pull ();
	
	int push (ByteBuffer sink);
}
