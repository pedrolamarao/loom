package loom.ber;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DerPullParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final ContinuationScope scope;
	
	private final Continuation continuation;
	
	private int consumed = 0;
	
	private DerPart next;
	
	private InputStream source;
	
	public DerPullParser ()
	{
		this.scope = new ContinuationScope("DerPullParser");
		this.continuation = new Continuation(scope, this::run);
	}
	
	public int consumed ()
	{
		return consumed;
	}
	
	public DerPart pull (InputStream in)
	{		
		next = null;
		source = in;
		continuation.run();
		source = null;
		return next;
	}
	
	private void run ()
	{
		int byte_ = 0;
		
		while (true) try
		{
			// tag
			
			final byte type = pull();

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
			
			byte_ = pull();
			
			if ((byte_ & 0x80) == 0) {
				// definite short length
				length = byte_;
			}
			else {
				if (byte_ != 0x80) {
					int tmp = 0;
					for (int i = 0, j = (byte_ & 0x7F); i != j; ++i) {
						tmp <<= 8;
						tmp |= (pull() & 0xFF);
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
				pull(content); // #TODO: do not copy! reuse!
				content.flip();
				
				logger.atDebug().log("parser [{}]: consumed = {}", hashCode(), consumed);
				
				yield_(new DerPrimitive(type, tag, content));
			}
			else
			{
				// constructed
				
				yield_(new DerOpenConstructed(type, tag, length));
				
				final var parser = new DerPullParser();
				
				while (parser.consumed < length)
				{
					final var part = parser.pull(source);
					yield_(part);
				}
				
				consumed += parser.consumed;
				
				yield_(new DerCloseConstructed(type, tag, length));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private byte pull () throws IOException
	{
		while (true) 
		{
			final int byte_ = source.read();
			if (byte_ == -1) {
				logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
				Continuation.yield(scope);
				continue;
			}
			
			++consumed;
			logger.atTrace().log("parser [{}]: pull from {} value {}", hashCode(), source.hashCode(), byte_);
			return (byte) byte_;
		}
	}
	
	private void pull (ByteBuffer sink) throws IOException
	{
		while (sink.hasRemaining()) 
		{
			final int byte_ = source.read();
			if (byte_ == -1) {
				logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
				Continuation.yield(scope);
				continue;
			}

			++consumed;
			logger.atTrace().log("parser [{}]: pull from {} value {}", hashCode(), source.hashCode(), byte_);
			sink.put((byte) byte_);
		}
	}
	
	private void yield_ (DerPart thing)
	{
		next = thing;
		Continuation.yield(scope);
	}
}
