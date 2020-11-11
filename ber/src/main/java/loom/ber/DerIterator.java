package loom.ber;

import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DerIterator implements Iterator<DerPart>
{
	private DerPart next;
	
	private final DerPullParser parser;
	
	private final InputStream source;
	
	public DerIterator (InputStream source)
	{
		this.parser = new DerPullParser();
		this.source = source;
	}

	@Override
	public boolean hasNext ()
	{
		if (next == null)
			next = parser.pull(source);
		
		return (next != null);
	}

	@Override
	public DerPart next ()
	{
		var tmp = next;
		next = null;
		if (tmp == null)
			tmp = parser.pull(source);
		if (tmp == null)
			throw new NoSuchElementException();
		return tmp;
	}
}
