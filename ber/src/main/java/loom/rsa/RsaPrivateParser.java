package loom.rsa;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loom.Generator;
import loom.ber.DerPart;
import loom.ber.DerPrimitive;

public final class RsaPrivateParser
{
	private static final Logger logger = LogManager.getLogger();
	
	private final Generator<RsaPart> generator;
	
	private Supplier<DerPart> source;
	
	public RsaPrivateParser ()
	{
		this.generator = new Generator<>(this::run);
	}
	
	public RsaPart parse (Supplier<DerPart> source)
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
			final var versionNumber = bigInteger(version.content());
			yield.accept(new RsaPart(RsaPartType.VERSION, versionNumber));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive modulus))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.MODULUS, bigInteger(modulus.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive publicExponent))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.PUBLIC_EXPONENT, bigInteger(publicExponent.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive privateExponent))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.PRIVATE_EXPONENT, bigInteger(privateExponent.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive prime_1))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.PRIME_1, bigInteger(prime_1.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive prime_2))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.PRIME_2, bigInteger(prime_2.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive exponent_1))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.EXPONENT_1, bigInteger(exponent_1.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive exponent_2))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.EXPONENT_2, bigInteger(exponent_2.content())));
			
			part = pull(yield);
			if (part.tag() != 2)
				throw new RuntimeException("expected INTEGER");
			if (! (part instanceof DerPrimitive coefficient))
				throw new RuntimeException("expected primitive INTEGER");
			yield.accept(new RsaPart(RsaPartType.COEFFICIENT, bigInteger(coefficient.content())));

			if (! versionNumber.equals(BigInteger.ZERO)) {
				throw new RuntimeException("run: version not supported: " + versionNumber);
			}
			
			final var closeSequence = pull(yield);
			if (closeSequence.tag() != 16)
				throw new RuntimeException("expected close SEQUENCE");
		}
	}
	
	private DerPart pull (Consumer<?> yield)
	{
		DerPart part = null;
		do {
			part = source.get();
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
