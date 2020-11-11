package loom.akp;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.ber.DerCloseConstructed;
import loom.ber.DerOpenConstructed;
import loom.ber.DerPart;
import loom.ber.DerPrimitive;
import loom.ber.DerPullParser;

public final class PrivateKeyInfoPullParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final Continuation continuation;
	
	private final DerPullParser parser;

	private final ContinuationScope scope = new ContinuationScope("PrivateKeyInfoPullParser");
	
	private PrivateKeyInfoPart next;
	
	private InputStream source;

	public PrivateKeyInfoPullParser ()
	{
		this.continuation = new Continuation(scope, this::run);
		this.parser = new DerPullParser();
	}
	
	public PrivateKeyInfoPart pull (InputStream source)
	{
		this.next = null;
		this.source = source;
		continuation.run();
		return next;
	}
	
	private void run ()
	{
		while (true)
		{
			// open privateKeyInfo
			pullOpen(16);

			// privateKeyInfo.version
			final var version = pullPrimitive(2);
			this.yield(new PrivateKeyInfoVersion(bigInteger(version.content())));
			
			// open privateKeyInfo.privateKeyAlgorithm
			pullOpen(16);

			// privateKeyInfo.privateKeyAlgorithm.algorithm
			final var algorithm = pullPrimitive(6);
			this.yield(new PrivateKeyInfoAlgorithm(algorithm.content()));
			
			// privateKeyInfo.privateKeyAlgorithm.parameters
			pull();
			
			pullClose(16);
			// close privateKeyInfo.privateKeyAlgorithm

			final var privateKey = pullPrimitive(4);
			this.yield(new PrivateKeyInfoPrivateKey(privateKey.content()));

			pullClose(16);
			// close privateKeyInfo
		}
	}
	
	@SuppressWarnings("preview")
	private DerCloseConstructed pullClose (int tag)
	{
		final var part = pull();
		if (! (part instanceof DerCloseConstructed close))
			throw new RuntimeException("unexpected close: " + part);
		if (close.tag() != tag)
			throw new RuntimeException("unexpected tag: " + close.tag());
		return close;
	}
	
	@SuppressWarnings("preview")
	private DerOpenConstructed pullOpen (int tag)
	{
		final var part = pull();
		if (! (part instanceof DerOpenConstructed open))
			throw new RuntimeException("expected open: " + part);
		if (open.tag() != tag)
			throw new RuntimeException("expected tag: " + open.tag());
		return open;
	}
	
	@SuppressWarnings("preview")
	private DerPrimitive pullPrimitive (int tag)
	{
		final var part = pull();
		if (! (part instanceof DerPrimitive primitive))
			throw new RuntimeException("unexpected primitive: " + part);
		if (primitive.tag() != tag)
			throw new RuntimeException("unexpected tag: " + primitive.tag());
		return primitive;
	}
	
	private DerPart pull ()
	{
		DerPart part = null;
		do {
			part = parser.pull(source);
			if (part == null) {
				logger.atTrace().log("pull [{}]: got null, yielding", hashCode());
				Continuation.yield(scope);
			}
		}
		while (part == null);
		logger.atTrace().log("pull [{}]: got {}", hashCode(), part);
		return part;
	}
	
	private void yield (PrivateKeyInfoPart next)
	{
		this.next = next;
		logger.atDebug().log("run [{}]: next {}", hashCode(), next);
		Continuation.yield(scope);
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
