# java.util.Random-cracker
A program which demonstrates the insecurities of java.util.Random. A single randomly generated 64-bit long is enough to determine the random object's current state and predict every future output.

Sample run:
Initializing java.util.Random()
Random::nextLong() returned -664121283405651921
Recovered seed: 33886197482178
Cloning random...

     Random      Cloned
-1421665289 -1421665289
-1064390867 -1064390867
 -477380420  -477380420
  865866843   865866843
 1699363762  1699363762