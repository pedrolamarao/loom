package loom;

import java.util.function.Supplier;

public class Generator <T> implements Supplier<T>
{	
	private final Continuation continuation;

	private final GeneratorRoutine<T> routine;
	
	private final ContinuationScope scope;
	
	private T next;
	
	public Generator (GeneratorRoutine<T> routine)
	{
		this.routine = routine;
		this.scope = new ContinuationScope("loom.Generator");
		this.continuation = new Continuation(scope, this::run);
	}
	
	public Generator (GeneratorRoutine<T> routine, int stack)
	{
		this.routine = routine;
		this.scope = new ContinuationScope("loom.Generator");
		this.continuation = new Continuation(scope, stack, this::run);
	}
	
	public T get ()
	{
		next = null;
		continuation.run();
		return next;
	}
	
	private void run ()
	{
		routine.run(this::yield);
	}
	
	private void yield (T value)
	{
		next = value;
		Continuation.yield(scope);
	}
}
