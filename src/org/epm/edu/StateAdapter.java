package org.epm.edu;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.epm.edu.statechanges.IStateChange;

/**
 * Convenience class for smaller state implementations
 */
public abstract class StateAdapter implements State{

	public abstract IStateChange feed(int c);

	/**
	 * Get the default name of this node: the 
	 * class name.
	 * 
	 * @return The class name of the overriding class
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public void reset() { }

	/**
	 * Copy this state by invoking the constructor without
	 * parameters and copying over all fields.
	 * If the class does not have a zero argument constructor
	 * this method will have to be overwritten.
	 * 
	 * @return A copy of this state
	 */
	public State copy(){
		Constructor<?>[] constructors = getClass().getDeclaredConstructors();
		State out = null;
		for (Constructor<?> c : constructors){
			if (c.getParameterTypes().length == 0)
				try {
					c.setAccessible(true);
					out = (State) c.newInstance();
				} catch (Exception e) {
					continue;
				}
		}
		if (out == null)
			throw new RuntimeException("Could not use generic zero-parameter constructor: Implement " + getClass().getSimpleName() + ".copy()!");
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields){
			try{
				f.setAccessible(true);
				Object value = f.get(this);
				f.set(out, value);
			} catch (Exception e){
				throw new RuntimeException("Could not copy field(s): Implement " + getClass().getSimpleName() + ".copy()!");
			}
		}
		return out;
	}
	
}
