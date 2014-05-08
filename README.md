------------------
# EasyParseMachine (EPM)
------------------
### Introduction
Over the years I have created many custom file formats which were
read using a `java.util.Scanner`. Now, since I did not want to
create a formal grammar just for a simple file format and also 
wanted more flexibility than a simple scanner could give me: I 
created EasyParseMachine.

You can think of EPM as a parse tree with local states.

See [JSONReader](https://github.com/qstokkink/EasyParseMachine/blob/master/src/test/JSONReader.java)
for a complete example on how EPM can be used.

### State transitions
There exist 7 state transitions, as specified in the following
table and explained in their corresponding subsections.

<table>
	<tr>
		<th>Transition</th><th>Next state</th><th>Consume input?</th>
	</tr>
	<tr>
		<td>Accept</td><td>return</td><td>No</td>
	</tr>
	<tr>
		<td>Closure</td><td>return</td><td>Yes</td>
	</tr>
	<tr>
		<td>Consume</td><td>self</td><td>Yes</td>
	</tr>
	<tr>
		<td>Fail</td><td>-</td><td>-</td>
	</tr>
	<tr>
		<td>Goto</td><td>as specified</td><td>No</td>
	</tr>
	<tr>
		<td>Guess</td><td>as specified</td><td>No</td>
	</tr>
	<tr>
		<td>Split</td><td>as specified</td><td>Yes</td>
	</tr>
</table>

------------------
#### Accept
__Use pattern:__ Consume X until Y <br>
__Input consumed:__ no <br>
__Tree transition:__ go to parent node <br>
__State transition:__ go to state corresponding to parent node <br>
__Example:__
```Java
public IStateChange feed(int c) {
	if (Character.isDigit(c)) {
		result += (char) c;
		return new Consume();
	} else {
		return new Accept(result);
	}
}
```
------------------
#### Closure
__Use pattern:__ End input with an X <br>
__Input consumed:__ yes <br>
__Tree transition:__ go to parent node <br>
__State transition:__ go to state corresponding to parent node <br>
__Example:__
```Java
public IStateChange feed(int c) {
	if (c == ')')
		return new Closure();
	else
		return new Fail();
}
```
------------------
#### Consume
__Use pattern:__ Greedy match all input of type X <br>
__Input consumed:__ yes <br>
__Tree transition:__ stay in the same node <br>
__State transition:__ stay in the same state <br>
__Example:__ see [Accept](#accept)

------------------
#### Fail
__Use pattern:__ If not conform to language, error out <br>
__Input consumed:__ - <br>
__Tree transition:__ - <br>
__State transition:__ exit execution <br>
__Example:__ see [Closure](#closure)

------------------
#### Goto
__Use pattern:__ Start matching with state X <br>
__Input consumed:__ no <br>
__Tree transition:__ new child node <br>
__State transition:__ go to specified state <br>
__Example:__ 
```Java
public IStateChange feed(int c) {
	if (c == '(')
		return new Consume();
	else
		return new Goto("ClosingParenthesis");
}
```
------------------
#### Guess
__Use pattern:__ Start matching with state X or Y <br>
__Input consumed:__ no <br>
__Tree transition:__ new child node, whichever state change does not fail gets to stay <br>
__State transition:__ go to specified states in parallel <br>
__Example:__ 
```Java
public IStateChange feed(int c) {
	if (c == '(')
		return new Consume();
	else
		return new Guess("ClosingParenthesis", "Digits", "Letters");
}
```
------------------
#### Split
__Use pattern:__ Upon reading X: start matching with state Y or Z <br>
__Input consumed:__ yes <br>
__Tree transition:__ new child node, whichever state change does not fail gets to stay <br>
__State transition:__ go to specified states in parallel <br>
__Example:__ 
```Java
public IStateChange feed(int c) {
	if (c == '(')
		return new Split("ClosingParenthesis", "Digits", "Letters");
	else
		return new Fail();
}
```
------------------
