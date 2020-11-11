package loom.ber.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import loom.ber.DerIterator;

public final class IteratorTest
{
	@Test
	public void rsa_f4_1024_p8 () throws Exception
	{
		try (var source = IteratorTest.class.getResourceAsStream("/rsa_f4_1024.p8"))
		{
			final var iterator = new DerIterator(source);
			
			// PrivateKeyInfo  OPEN
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.version : INTEGER
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(2, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier OPEN
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm.algorithm : OBJECT IDENTIFIER
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(6, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm.parameters : OPTIONAL
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(5, part.tag());
			}
			
			// PrivateKeyInfo.privateKeyAlgorithm : PrivateKeyAlgorithmIdentifier CLOSE
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
			
			// PrivateKeyInfo.privateKey : OCTET STRING
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(4, part.tag());
			}
			
			// PrivateKeyInfo CLOSE
			
			{
				assertTrue(iterator.hasNext());
				final var part = iterator.next();
				assertNotNull(part);
				assertEquals(16, part.tag());
			}
		}
	}
}
