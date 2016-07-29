Catch Test Runner for Eclipse CDT
=================================
A plug in to run the catch (https://github.com/philsquared/Catch) test framework within the eclipse CDT test runner.

Installation
============
Currently the plug in must be installed manually. To do this copy the `catch-test.jar` into the eclipse dropins folder. Hereafter start eclipse in a clean mode to refresh the installation.

```
cp catch-tester.jar <path/to/eclipse>/dropins
eclipse -clean
```
If you need root privilegs (eclipse is installed in /usr/lib/eclipse) then prepend the sudo command.

Now the "Catch Tests Runner" should be selectable in the C++ Unit Test Run Configuration.

*Hint: Runs only with java version 1.8*


Status
======
Beta: Tested with eclipse running on a Windows and Linux machine.

|Product | Version   |
|--------|-----------|
|Eclipse | 4.5 (Mars), 4.6 (Neon)|
|CDT     | 8.8       |
|java    | 1.8       |
