package loom;

import java.util.function.Consumer;

public interface GeneratorRoutine <T>
{
	void run (Consumer<T> yield);
}
