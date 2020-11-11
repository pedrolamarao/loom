package loom.akp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import loom.akp.PrivateKeyInfoAlgorithm;
import loom.akp.PrivateKeyInfoPart;
import loom.akp.PrivateKeyInfoPrivateKey;
import loom.akp.PrivateKeyInfoPullParser;
import loom.akp.PrivateKeyInfoVersion;

public final class PullParserTest
{
	@Test
	public void full () throws Exception
	{
		// prepare
		
		final var pair = KeyPairGenerator.getInstance("RSA").genKeyPair();
		
		final var source = new ByteArrayInputStream(pair.getPrivate().getEncoded());
		
		//
		
		final var parser = new PrivateKeyInfoPullParser();
		
		// privateKeyInfo.version
		
		{
			final var version = (PrivateKeyInfoVersion) parser.pull(source);
			assertEquals(BigInteger.ZERO, version.number());
		}
		
		{
			final var algorithm = (PrivateKeyInfoAlgorithm) parser.pull(source);
			assertNotNull(algorithm.identifier());
		}
		
		{
			final var privateKey = (PrivateKeyInfoPrivateKey) parser.pull(source);
			assertNotNull(privateKey.content());
		}
	}
	@Test
	public void half () throws Exception
	{
		// prepare
		
		final var pair = KeyPairGenerator.getInstance("RSA").genKeyPair();
		
		final var bytes = pair.getPrivate().getEncoded();
		
		final var left = new ByteArrayInputStream(bytes, 0, (bytes.length / 2));
		
		final var right = new ByteArrayInputStream(bytes, (bytes.length / 2), bytes.length - (bytes.length / 2));
		
		final var parts = new ArrayList<PrivateKeyInfoPart>();
		
		// execute
		
		final var parser = new PrivateKeyInfoPullParser();
		
		while (true) {
			final var part = parser.pull(left);
			if (part == null)
				break;
			parts.add(part);
		}
		
		while (true) {
			final var part = parser.pull(right);
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
