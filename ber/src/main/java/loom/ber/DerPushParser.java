package loom.ber;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.Generator;

public final class DerPushParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final Generator<DerPart> generator;
	
	private int consumed = 0;
	
	private ByteBuffer source;
	
	public DerPushParser ()
	{
		this.generator = new Generator<DerPart>(this::run);
	};
	
	public int consumed ()
	{
		return consumed;
	}
	
	public DerPart push (ByteBuffer in)
	{
		source = in;		
		final var next = generator.get();	
		source = null;		
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
				pull(content, yield); // #TODO: do not copy! reuse!
				content.flip();
				
				logger.atDebug().log("parser [{}]: consumed = {}", hashCode(), consumed);
				
				yield.accept(new DerPrimitive(type, tag, content));
			}
			else
			{
				// constructed
				
				yield.accept(new DerOpenConstructed(type, tag, length));
				
				final var parser = new DerPushParser();
				
				while (parser.consumed < length)
				{
					final var part = parser.push(source);
					yield.accept(part);
				}
				
				consumed += parser.consumed;
				
				yield.accept(new DerCloseConstructed(type, tag, length));
			}
		}
	}
	
	public byte pull (Consumer<?> yield)
	{
		if (! source.hasRemaining()) {
			logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
			yield.accept(null);
		}
		
		final byte byte_ = source.get();
		++consumed;
		logger.atTrace().log("parser [{}]: pull from {} position {} value {}", hashCode(), source.hashCode(), (source.position() - 1), byte_);
		return byte_;
	}
	
	public void pull (ByteBuffer sink, Consumer<?> yield)
	{
		while (sink.hasRemaining()) 
		{
			if (! source.hasRemaining()) {
				logger.atTrace().log("parser [{}]: pull from {}, empty", hashCode(), source.hashCode());
				yield.accept(null);
			}

			++consumed;
			final byte byte_ = source.get();
			logger.atTrace().log("parser [{}]: pull from {} position {} value {}", hashCode(), source.hashCode(), (source.position() - 1), byte_);
			sink.put(byte_);
		}
	}
}
