package loom.ber;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DerPushParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final ContinuationScope scope;
	
	private final Continuation continuation;
	
	private int consumed = 0;
	
	private DerPart next;
	
	private ByteBuffer source;
	
	public DerPushParser ()
	{
		this.scope = new ContinuationScope("DerPushParser");
		this.continuation = new Continuation(scope, this::run);
	}
	
	public int consumed ()
	{
		return consumed;
	}
	
	public DerPart push (ByteBuffer in)
	{		
		next = null;
		source = in;		
		continuation.run();		
		source = null;		
		return next;
	}
	
	public void run ()
	{
		byte byte_ = 0;
		
		while (true)
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
				
				final var parser = new DerPushParser();
				
				while (parser.consumed < length)
				{
					final var part = parser.push(source);
					yield_(part);
				}
				
				consumed += parser.consumed;
				
				yield_(new DerCloseConstructed(type, tag, length));
			}
		}
	}
	
	public byte pull ()
	{
		if (! source.hasRemaining()) {
			logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
			Continuation.yield(scope);
		}
		
		final byte byte_ = source.get();
		++consumed;
		logger.atTrace().log("parser [{}]: pull from {} position {} value {}", hashCode(), source.hashCode(), (source.position() - 1), byte_);
		return byte_;
	}
	
	public void pull (ByteBuffer sink)
	{
		while (sink.hasRemaining()) 
		{
			if (! source.hasRemaining()) {
				logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
				Continuation.yield(scope);
			}

			++consumed;
			final byte byte_ = source.get();
			logger.atTrace().log("parser [{}]: pull from {} position {} value {}", hashCode(), source.hashCode(), (source.position() - 1), byte_);
			sink.put(byte_);
		}
	}
	
	public void yield_ (DerPart thing)
	{
		next = thing;
		Continuation.yield(scope);
	}
}
