package loom.ber.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyFactory;

import org.junit.jupiter.api.Test;

import loom.ber.DerPullParser;

public final class PullParserTest
{
	@Test
	public void rsa_f4_1024_p8 () throws Exception
	{
		final var parser = new DerPullParser();
		
		try (var source = PullParserTest.class.getResourceAsStream("/rsa_f4_1024.p8"))
		{
			// PrivateKeyInfo  OPEN
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.version : INTEGER
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(2, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier OPEN
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm.algorithm : OBJECT IDENTIFIER
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(6, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm.parameters : OPTIONAL
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(5, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier CLOSE
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.privateKey : OCTET STRING
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(4, part.tag());
				
				KeyFactory.getInstance("RSA");
			}
			
			// PrivateKeyInfo CLOSE
			
			{
				final var part = parser.pull(source);
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
		}
	}
}
