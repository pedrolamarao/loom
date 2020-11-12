package loom.rsa;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.Generator;
import loom.ber.DerParser;
import loom.ber.DerPart;
import loom.ber.DerPrimitive;

public final class RsaPrivatePullParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final Generator<RsaPart> generator;

	private final DerParser parser;
	
	private InputStream source;
	
	public RsaPrivatePullParser ()
	{
		this.generator = new Generator<>(this::run);
		this.parser = new DerParser();;
	}
	
	public RsaPart pull (InputStream source)
	{
		this.source = source;
		final var next = generator.get();
		this.source = null;
		return next;
	}
	
	@SuppressWarnings("preview")
	private void run (Consumer<RsaPart> yield)
	{
		while (true)
		{
			var part = pull(yield);
			if (part.tag() != 16)
				throw new RuntimeException("expected open SEQUENCE");
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive version))
				throw new RuntimeException("expected primitive INTEGER");
			else
				yield.accept(new RsaPart(RsaPartType.VERSION, bigInteger(version.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive modulus))
				throw new RuntimeException("expected primitive INTEGER");
			else
				yield.accept(new RsaPart(RsaPartType.MODULUS, bigInteger(modulus.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive publicExponent))
				throw new RuntimeException("expected primitive INTEGER");
			else
				yield.accept(new RsaPart(RsaPartType.PUBLIC_EXPONENT, bigInteger(publicExponent.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive privateExponent))
				throw new RuntimeException("expected primitive INTEGER");
			else
				yield.accept(new RsaPart(RsaPartType.PUBLIC_EXPONENT, bigInteger(privateExponent.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			// prime1
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			// prime2
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			// exponent1
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			// exponent2
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			// coefficient
			
			// #TODO: other prime infos

			final var closeSequence = pull(yield);
			if (closeSequence.tag() != 16)
				throw new RuntimeException("expected close SEQUENCE");
		}
	}
	
	private DerPart pull (Consumer<?> yield)
	{
		DerPart part = null;
		do {
			part = parser.parse(source);
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
