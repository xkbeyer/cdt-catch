Catch Test Runner for Eclipse CDT
=================================
A plug in to run the catch (https://github.com/philsquared/Catch) test framework within the eclipse CDT test runner.

Installation
============
Currently the plug in must be installed manually. To do this, copy the `catch-testrunner.jar` (see releases) into the eclipse dropins folder. Hereafter start eclipse in a clean mode to refresh the installation.

```
cp catch-testrunner.jar <path/to/eclipse>/dropins/.
eclipse -clean
```
If you need root rights (eclipse is installed in /usr/lib/eclipse), prefix the command sudo.

Now the "Catch Tests Runner" should be selectable in the "C++ Unit Test Run" configuration.

*Hint: Runs only with Java version 1.8*


Status
======
Tested with Eclipse on Windows and Linux.

|Product | Version   |
|--------|-----------|
|Catch   | 1.10.0, 2.1.2 |
|Eclipse | 4.5 (Mars), 4.6 (Neon), 4.7 (Oxygen)|
|CDT     | 8.8 - 9.3 |
|Java    | 1.8       |
