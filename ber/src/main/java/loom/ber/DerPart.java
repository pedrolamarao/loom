package loom.ber;

@SuppressWarnings("preview")
public sealed interface DerPart 
	permits DerCloseConstructed, DerOpenConstructed, DerPrimitive
{
	byte type ();
	
	int tag ();
	
	int length ();
}
