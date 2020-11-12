package loom.ber.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import loom.ber.DerParser;
import loom.ber.DerPart;

public final class ParserTest
{
	@Test
	public void rsa_f4_1024_p8__buffer__full () throws Exception
	{
		// prepare
		
		final List<DerPart> parts = new ArrayList<DerPart>();

		final ByteBuffer source;
		
		try (var stream = ParserTest.class.getResourceAsStream("/rsa_f4_1024.p8"))
		{
			final byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			source = ByteBuffer.wrap(bytes);
		}
		
		// execute
		
		final var parser = new DerParser();
		
		while (source.hasRemaining()) {
			while (true) {
				final var part = parser.parse(source);
				if (part == null)
					break;
				parts.add(part);
			}
		}
		
		// verify
		
		// PrivateKeyInfo  OPEN
		
		{
			final var part = parts.get(0);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}
		
		// PrivateKeyInfo.version : INTEGER
		
		{
			final var part = parts.get(1);
			assertNotNull(part);
			assertEquals(2, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier OPEN
		
		{
			final var part = parts.get(2);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm.algorithm : OBJECT IDENTIFIER
		
		{
			final var part = parts.get(3);
			assertNotNull(part);
			assertEquals(6, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm.parameters : OPTIONAL
		
		{
			final var part = parts.get(4);
			assertNotNull(part);
			assertEquals(5, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier CLOSE
		
		{
			final var part = parts.get(5);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}
		
		// PrivateKeyInfo.privateKey : OCTET STRING
		
		{
			final var part = parts.get(6);
			assertNotNull(part);
			assertEquals(4, part.tag());
		}
		
		// PrivateKeyInfo CLOSE
		
		{
			final var part = parts.get(7);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}
	}
	
	@Test
	public void rsa_f4_1024_p8__buffer__smallest () throws Exception
	{
		// prepare
		
		final var parts = new ArrayList<DerPart>();
		
		final byte[] bytes;
		
		try (var source = ParserTest.class.getResourceAsStream("/rsa_f4_1024.p8"))
		{
			bytes = new byte[source.available()];
			source.read(bytes);
		}
		
		// execute
		
		final var parser = new DerParser();
		
		for (int i = 0, j = bytes.length; i != j; ++i) 
		{
			final var source = ByteBuffer.wrap(bytes, i, 1);
			while (source.hasRemaining()) {
				while (true) {
					final var part = parser.parse(source);
					if (part == null)
						break;
					parts.add(part);
				}
			}
		}
		
		// verify
		
		// PrivateKeyInfo OPEN
		
		{
			final var part = parts.get(0);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}
		
		// PrivateKeyInfo.version : INTEGER
		
		{
			final var part = parts.get(1);
			assertNotNull(part);
			assertEquals(2, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier OPEN
		
		{
			final var part = parts.get(2);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm.algorithm : OBJECT IDENTIFIER
		
		{
			final var part = parts.get(3);
			assertNotNull(part);
			assertEquals(6, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm.parameters : OPTIONAL
		
		{
			final var part = parts.get(4);
			assertNotNull(part);
			assertEquals(5, part.tag());
		}
		
		// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier CLOSE
		
		{
			final var part = parts.get(5);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}
		
		// PrivateKeyInfo.privateKey : OCTET STRING
		
		{
			final var part = parts.get(6);
			assertNotNull(part);
			assertEquals(4, part.tag());
		}
		
		// PrivateKeyInfo CLOSE
		
		{
			final var part = parts.get(7);
			assertNotNull(part);
			assertEquals(16, part.tag());
		}		
	}

	@Test
	public void rsa_f4_1024_p8__stream () throws Exception
	{
		final var parser = new DerParser();
		
		try (var source = PullParserTest.class.getResourceAsStream("/rsa_f4_1024.p8"))
		{
			// PrivateKeyInfo  OPEN
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.version : INTEGER
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(2, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier OPEN
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm.algorithm : OBJECT IDENTIFIER
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(6, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm.parameters : OPTIONAL
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(5, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier CLOSE
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.privateKey : OCTET STRING
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(4, part.tag());
				
				KeyFactory.getInstance("RSA");
			}
			
			// PrivateKeyInfo CLOSE
			
			{
				final var part = parser.parse(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
		}
	}
}
