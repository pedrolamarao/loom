package loom.rsa;

import java.math.BigInteger;

@SuppressWarnings("preview")
public record RsaPart(RsaPartType type, BigInteger value)
{

}
