# java.util.Random-cracker
A program which demonstrates the insecurities of java.util.Random. A single randomly generated 64-bit long is enough to determine the random object's current state and predict every future output. Alternativly, two 32-bit integers or a 64-bit double (only 53-bits are randomly generated) can be used to determine the seed and clone the random number generator.

## Sample Runs
### NotRandom.java
![image](https://user-images.githubusercontent.com/58671117/123203004-aa6ce680-d483-11eb-861f-5c2c890d767a.png)
### NotRandomDouble.java
![image](https://user-images.githubusercontent.com/58671117/123207379-4cdc9800-d48b-11eb-833b-a2502faab1b1.png)

