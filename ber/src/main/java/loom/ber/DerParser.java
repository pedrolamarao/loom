package loom.ber;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.Generator;
import loom.ber.internal.BufferSource;
import loom.ber.internal.Source;
import loom.ber.internal.StreamSource;
import loom.ber.internal.ChannelSource;

public final class DerParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final Generator<DerPart> generator;
	
	private int consumed = 0;
	
	private Source source;
	
	public DerParser ()
	{
		this.generator = new Generator<DerPart>(this::run);
	};
	
	public int consumed ()
	{
		return consumed;
	}
	
	public DerPart parse (ByteBuffer source)
	{
		return parse(new BufferSource(source));
	}
	
	public DerPart parse (InputStream source)
	{
		return parse(new StreamSource(source));
	}
	
	public DerPart parse (ReadableByteChannel source)
	{
		return parse(new ChannelSource(source));
	}
	
	private DerPart parse (Source source)
	{
		this.source = source;		
		final var next = generator.get();	
		this.source = null;		
		return next;
	}
	
	public void run (Consumer<DerPart> yield)
	{
		byte byte_ = 0;
		
		while (true)
		{
			// tag
			
			final byte type = pull(yield);
			
			logger.atDebug().log("parser [{}]: type = {}", hashCode(), type);
			
			final int tag;
			
			if ((type & 0x1F) != 0x1F) {
				// low tag
				tag = (type & 0x1F);
			}
			else {
				// #TODO: high tag
				throw new RuntimeException("high tag is unsupported");
			}
			
			logger.atDebug().log("parser [{}]: tag = {}", hashCode(), tag);
			
			// length
			
			final int length;
			
			byte_ = pull(yield);
			
			if ((byte_ & 0x80) == 0) {
				// definite short length
				length = byte_;
			}
			else {
				if (byte_ != 0x80) {
					int tmp = 0;
					for (int i = 0, j = (byte_ & 0x7F); i != j; ++i) {
						tmp <<= 8;
						tmp |= (pull(yield) & 0xFF);
					}
					length = tmp;
				}
				else {
					// #TODO: indefinite length
					throw new RuntimeException("indefinite length is unsupported");
				}
			}
			
			logger.atDebug().log("parser [{}]: length = {}", hashCode(), length);
			
			// object
			
			if ((type & 0x20) == 0)
			{
				// primitive
				
				final var content = ByteBuffer.allocate(length);
				push(content, yield); // #TODO: do not copy! reuse!
				content.flip();
				
				logger.atDebug().log("parser [{}]: consumed = {}", hashCode(), consumed);
				
				yield.accept(new DerPrimitive(type, tag, content));
			}
			else
			{
				// constructed
				
				yield.accept(new DerOpenConstructed(type, tag, length));
				
				final var parser = new DerParser();
				
				while (parser.consumed < length)
				{
					final var part = parser.parse(source);
					yield.accept(part);
				}
				
				consumed += parser.consumed;
				
				yield.accept(new DerCloseConstructed(type, tag, length));
			}
		}
	}
	
	private byte pull (Consumer<?> yield)
	{
		int byte_ = 0;
		
		while ((byte_ = source.pull()) == -1) {
			yield.accept(null);
		}

		++consumed;
		return (byte) byte_;
	}
	
	private void push (ByteBuffer sink, Consumer<?> yield)
	{
		while (sink.hasRemaining()) 
		{
			final int pulled = source.push(sink);
			if (pulled == -1) {
				yield.accept(null);
			}
			else {
				consumed += pulled;
			}
		}
	}
}
