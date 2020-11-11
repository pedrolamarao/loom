package loom.ber;

@SuppressWarnings("preview")
public final record DerOpenConstructed(byte type, int tag, int length) implements DerPart {

}
