package loom.akp;

@SuppressWarnings("preview")
public sealed interface PrivateKeyInfoPart
	permits PrivateKeyInfoAlgorithm, PrivateKeyInfoPrivateKey, PrivateKeyInfoVersion
{

}
