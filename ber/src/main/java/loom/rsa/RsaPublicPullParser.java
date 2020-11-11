package loom.rsa;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.ber.DerPart;
import loom.ber.DerPrimitive;
import loom.ber.DerPullParser;

public final class RsaPublicPullParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final ContinuationScope continuationScope;
	
	private final Continuation continuation;

	private final DerPullParser parser;
	
	private RsaPart next;
	
	private InputStream source;
	
	public RsaPublicPullParser ()
	{
		this.continuationScope = new ContinuationScope("RsaPublicPullParser");
		this.continuation = new Continuation(continuationScope, this::run);
		this.parser = new DerPullParser();
	}
	
	public RsaPart pull (InputStream source)
	{
		this.next = null;
		this.source = source;
		continuation.run();
		this.source = null;
		return this.next;
	}
	
	@SuppressWarnings("preview")
	private void run ()
	{
		while (true)
		{
			var part = pull();
			if (part.tag() != 16)
				throw new RuntimeException("expected open SEQUENCE");
			
			part = pull();
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive modulus))
				throw new RuntimeException("expected primitive");
			else
				yield_(new RsaPart(RsaPartType.MODULUS, bigInteger(modulus.content())));
			
			part = pull();
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive exponent))
				throw new RuntimeException("expected primitive");
			else
				yield_(new RsaPart(RsaPartType.PUBLIC_EXPONENT, bigInteger(exponent.content())));

			final var closeSequence = pull();
			if (closeSequence.tag() != 16)
				throw new RuntimeException("expected close SEQUENCE");
		}
	}
	
	private DerPart pull ()
	{
		DerPart part = null;
		do {
			part = parser.pull(source);
			if (part == null) {
				logger.atTrace().log("pull [{}]: got null, yielding", hashCode());
				Continuation.yield(continuationScope);
			}
		}
		while (part == null);
		logger.atTrace().log("pull [{}]: got {}", hashCode(), part);
		return part;
	}
	
	public void yield_ (RsaPart part)
	{
		next = part;
		Continuation.yield(continuationScope);
	}
	
	private static BigInteger bigInteger (ByteBuffer buffer)
	{
		if (buffer.isDirect()) {
			final byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			return new BigInteger(bytes);
		}
		else {
			return new BigInteger(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
		}
	}
}
