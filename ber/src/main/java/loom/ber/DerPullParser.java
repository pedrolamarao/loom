package loom.ber;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.Generator;

public final class DerPullParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final Generator<DerPart> generator;
	
	private int consumed = 0;
	
	private InputStream source;
	
	public DerPullParser ()
	{
		this.generator = new Generator<DerPart>(this::run);
	}
	
	public int consumed ()
	{
		return consumed;
	}
	
	public DerPart pull (InputStream in)
	{
		source = in;
		final var next = generator.get();
		source = null;
		return next;
	}
	
	private void run (Consumer<DerPart> yield)
	{
		int byte_ = 0;
		
		while (true) try
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
				pull(content, yield); // #TODO: do not copy! reuse!
				content.flip();
				
				logger.atDebug().log("parser [{}]: consumed = {}", hashCode(), consumed);
				
				yield.accept(new DerPrimitive(type, tag, content));
			}
			else
			{
				// constructed
				
				yield.accept(new DerOpenConstructed(type, tag, length));
				
				final var parser = new DerPullParser();
				
				while (parser.consumed < length)
				{
					final var part = parser.pull(source);
					yield.accept(part);
				}
				
				consumed += parser.consumed;
				
				yield.accept(new DerCloseConstructed(type, tag, length));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private byte pull (Consumer<DerPart> yield) throws IOException
	{
		while (true) 
		{
			final int byte_ = source.read();
			if (byte_ == -1) {
				logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
				yield.accept(null);
				continue;
			}
			
			++consumed;
			logger.atTrace().log("parser [{}]: pull from {} value {}", hashCode(), source.hashCode(), byte_);
			return (byte) byte_;
		}
	}
	
	private void pull (ByteBuffer sink, Consumer<DerPart> yield) throws IOException
	{
		while (sink.hasRemaining()) 
		{
			final int byte_ = source.read();
			if (byte_ == -1) {
				logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
				yield.accept(null);
				continue;
			}

			++consumed;
			logger.atTrace().log("parser [{}]: pull from {} value {}", hashCode(), source.hashCode(), byte_);
			sink.put((byte) byte_);
		}
	}
}
