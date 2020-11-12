package loom.akp;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.Generator;
import loom.ber.DerCloseConstructed;
import loom.ber.DerOpenConstructed;
import loom.ber.DerPart;
import loom.ber.DerPrimitive;
import loom.ber.DerPullParser;

public final class PrivateKeyInfoPullParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final Generator<PrivateKeyInfoPart> generator;
	
	private final DerPullParser parser;
	
	private InputStream source;

	public PrivateKeyInfoPullParser ()
	{
		this.generator = new Generator<>(this::run);
		this.parser = new DerPullParser();
	}
	
	public PrivateKeyInfoPart pull (InputStream source)
	{
		this.source = source;
		final var next = generator.get();
		this.source = null;
		return next;
	}
	
	private void run (Consumer<PrivateKeyInfoPart> yield)
	{
		while (true)
		{
			// open privateKeyInfo
			pullOpen(16, yield);

			// privateKeyInfo.version
			final var version = pullPrimitive(2, yield);
			yield.accept(new PrivateKeyInfoVersion(bigInteger(version.content())));
			
			// open privateKeyInfo.privateKeyAlgorithm
			pullOpen(16, yield);

			// privateKeyInfo.privateKeyAlgorithm.algorithm
			final var algorithm = pullPrimitive(6, yield);
			yield.accept(new PrivateKeyInfoAlgorithm(algorithm.content()));
			
			// privateKeyInfo.privateKeyAlgorithm.parameters
			pull(yield);
			
			pullClose(16, yield);
			// close privateKeyInfo.privateKeyAlgorithm

			final var privateKey = pullPrimitive(4, yield);
			yield.accept(new PrivateKeyInfoPrivateKey(privateKey.content()));

			pullClose(16, yield);
			// close privateKeyInfo
		}
	}
	
	@SuppressWarnings("preview")
	private DerCloseConstructed pullClose (int tag, Consumer<?> yield)
	{
		final var part = pull(yield);
		if (! (part instanceof DerCloseConstructed close))
			throw new RuntimeException("unexpected close: " + part);
		if (close.tag() != tag)
			throw new RuntimeException("unexpected tag: " + close.tag());
		return close;
	}
	
	@SuppressWarnings("preview")
	private DerOpenConstructed pullOpen (int tag, Consumer<?> yield)
	{
		final var part = pull(yield);
		if (! (part instanceof DerOpenConstructed open))
			throw new RuntimeException("expected open: " + part);
		if (open.tag() != tag)
			throw new RuntimeException("expected tag: " + open.tag());
		return open;
	}
	
	@SuppressWarnings("preview")
	private DerPrimitive pullPrimitive (int tag, Consumer<?> yield)
	{
		final var part = pull(yield);
		if (! (part instanceof DerPrimitive primitive))
			throw new RuntimeException("unexpected primitive: " + part);
		if (primitive.tag() != tag)
			throw new RuntimeException("unexpected tag: " + primitive.tag());
		return primitive;
	}
	
	private DerPart pull (Consumer<?> yield)
	{
		DerPart part = null;
		do {
			part = parser.pull(source);
			if (part == null) {
				logger.atTrace().log("pull [{}]: got null, yielding", hashCode());
				yield.accept(null);
			}
		}
		while (part == null);
		logger.atTrace().log("pull [{}]: got {}", hashCode(), part);
		return part;
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
