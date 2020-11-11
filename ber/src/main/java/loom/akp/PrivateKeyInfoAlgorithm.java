package loom.akp;

import java.nio.ByteBuffer;

@SuppressWarnings("preview")
public record PrivateKeyInfoAlgorithm(ByteBuffer identifier) implements PrivateKeyInfoPart {

}
