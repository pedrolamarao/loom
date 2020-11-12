package loom.akp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import loom.akp.PrivateKeyInfoAlgorithm;
import loom.akp.PrivateKeyInfoParser;
import loom.akp.PrivateKeyInfoPart;
import loom.akp.PrivateKeyInfoPrivateKey;
import loom.akp.PrivateKeyInfoVersion;
import loom.ber.DerParser;

public final class ParserTest
{
	@Test
	public void full () throws Exception
	{
		// prepare
		
		final var pair = KeyPairGenerator.getInstance("RSA").genKeyPair();
		
		final var stream = new ByteArrayInputStream(pair.getPrivate().getEncoded());
		
		final var derParser = new DerParser();

		
		//
		
		final var parser = new PrivateKeyInfoParser();
		
		// privateKeyInfo.version
		
		{
			final var version = (PrivateKeyInfoVersion) parser.parse(() -> derParser.parse(stream));
			assertEquals(BigInteger.ZERO, version.number());
		}
		
		{
			final var algorithm = (PrivateKeyInfoAlgorithm) parser.parse(() -> derParser.parse(stream));
			assertNotNull(algorithm.identifier());
		}
		
		{
			final var privateKey = (PrivateKeyInfoPrivateKey) parser.parse(() -> derParser.parse(stream));
			assertNotNull(privateKey.content());
		}
	}
	
	@Test
	public void half () throws Exception
	{
		// prepare
		
		final var pair = KeyPairGenerator.getInstance("RSA").genKeyPair();
		
		final var bytes = pair.getPrivate().getEncoded();
		
		final var mid = (bytes.length / 2);
		
		final var left = new ByteArrayInputStream(bytes, 0, mid);
		
		final var right = new ByteArrayInputStream(bytes, mid, bytes.length - mid);
		
		final var derParser = new DerParser();
		
		final var parts = new ArrayList<PrivateKeyInfoPart>();
		
		// execute
		
		final var parser = new PrivateKeyInfoParser();
		
		while (true) {
			final var part = parser.parse(() -> derParser.parse(left));
			if (part == null)
				break;
			parts.add(part);
		}
		
		while (true) {
			final var part = parser.parse(() -> derParser.parse(right));
			if (part == null)
				break;
			parts.add(part);
		}
		
		// verify
		
		{
			final var version = (PrivateKeyInfoVersion) parts.get(0);
			assertEquals(BigInteger.ZERO, version.number());
		}
		
		{
			final var algorithm = (PrivateKeyInfoAlgorithm) parts.get(1);
			assertNotNull(algorithm.identifier());
		}
		
		{
			final var privateKey = (PrivateKeyInfoPrivateKey) parts.get(2);
			assertNotNull(privateKey.content());
		}
	}
}
