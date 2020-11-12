package loom.rsa.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import loom.ber.DerParser;
import loom.rsa.RsaPart;
import loom.rsa.RsaPartType;
import loom.rsa.RsaPrivateParser;

public final class PrivateKeyParserTest
{
	@Test
	public void RSA_1024_010001__full () throws IOException
	{
		// prepare
		
		final var parts = new ArrayList<RsaPart>(); 
		
		try (var stream = PrivateKeyParserTest.class.getResourceAsStream("/RSA_1024_010001"))
		{

			final var derParser = new DerParser();
			
			// execute
			
			final var rsaParser = new RsaPrivateParser();
			
			while (true)
			{
				final var part = rsaParser.parse(() -> derParser.parse(stream));
				if (part == null)
					break;
				else
					parts.add(part);
			}
		}
		
		// verify
		
		assertEquals(9, parts.size());		
		assertEquals(RsaPartType.VERSION, parts.get(0).type());
		assertEquals(BigInteger.ZERO, parts.get(0).value());
		assertEquals(RsaPartType.MODULUS, parts.get(1).type());
		assertEquals(1024, parts.get(1).value().bitLength());
		assertEquals(RsaPartType.PUBLIC_EXPONENT, parts.get(2).type());
		assertEquals(RsaPartType.PRIVATE_EXPONENT, parts.get(3).type());
		assertEquals(RsaPartType.PRIME_1, parts.get(4).type());
		assertEquals(RsaPartType.PRIME_2, parts.get(5).type());
		assertEquals(RsaPartType.EXPONENT_1, parts.get(6).type());
		assertEquals(RsaPartType.EXPONENT_2, parts.get(7).type());
		assertEquals(RsaPartType.COEFFICIENT, parts.get(8).type());
	}
	@Test
	public void RSA_2048_010001__full () throws IOException
	{
		// prepare
		
		final var parts = new ArrayList<RsaPart>(); 
		
		try (var stream = PrivateKeyParserTest.class.getResourceAsStream("/RSA_2048_010001"))
		{

			final var derParser = new DerParser();
			
			// execute
			
			final var rsaParser = new RsaPrivateParser();
			
			while (true)
			{
				final var part = rsaParser.parse(() -> derParser.parse(stream));
				if (part == null)
					break;
				else
					parts.add(part);
			}
		}
		
		// verify
		
		assertEquals(9, parts.size());		
		assertEquals(RsaPartType.VERSION, parts.get(0).type());
		assertEquals(BigInteger.ZERO, parts.get(0).value());
		assertEquals(RsaPartType.MODULUS, parts.get(1).type());
		assertEquals(2048, parts.get(1).value().bitLength());
		assertEquals(RsaPartType.PUBLIC_EXPONENT, parts.get(2).type());
		assertEquals(RsaPartType.PRIVATE_EXPONENT, parts.get(3).type());
		assertEquals(RsaPartType.PRIME_1, parts.get(4).type());
		assertEquals(RsaPartType.PRIME_2, parts.get(5).type());
		assertEquals(RsaPartType.EXPONENT_1, parts.get(6).type());
		assertEquals(RsaPartType.EXPONENT_2, parts.get(7).type());
		assertEquals(RsaPartType.COEFFICIENT, parts.get(8).type());
	}
}
