package loom.akp;

import java.nio.ByteBuffer;

@SuppressWarnings("preview")
public record PrivateKeyInfoPrivateKey(ByteBuffer content) implements PrivateKeyInfoPart {

}
