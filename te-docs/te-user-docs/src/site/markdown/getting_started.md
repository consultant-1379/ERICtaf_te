<head>
    <title>Getting Started</title>
</head>

# Getting started

To start running tests using TAF TE you have to ensure that you have the following steps set up.

1. Prepare the [schedule][schedule description] XML for your tests.
This will define how your tests will be distributed.
2. In order to run the tests you'll need a Jenkins with TE trigger plugin installed, and a [TE vApp](te_vapp.html)
or a pool of [TE slaves for your Jenkins][TP description] that will carry out the actual task.
If you're an IT support representative, read the **Environment Set up** section of this documentation. If you're developer,
just talk to your manager - he/she should organize it for you.


## Preparing your testware

Please note that TAF test-pom will be completely ignored by TE.

So if you're using it for dependency management or to install some stuff that is needed for test run, your tests will
probably not run successfully in a TE run. According to TAF best practices, your test-pom should be just a way to execute
your tests. The test-pom should not change from what is provided by the archetype.

Need some Maven magic? Put it into testware POM (CXP module).

Need to pre-configure something? Turn it into the Java code and execute as a part of your testware - e.g.,
 create a test for it and put it into the test suite with `preserve-order=true` as the first one.


## Code requirements

TE does not put any requirements for code quality or TAF version. However, if you want to get the Allure reports
for your tests, make sure you're using the [latest version](https://taf.seli.wh.rnd.internal.ericsson.com/tafreleases/changelog.html) of TAF to benefit from the latest features.
Otherwise you will not get Allure reports for your tests.


## Same TAF version everywhere

Keep TAF version the same in all your testware that you're running in the same TE job.
If you have an old TAF version (without Allure support) in one module, and a new one in another, you will get frustrating results:
you will get Allure reports only for the second one. No one to blame except yourself.


[TP description]: te_trigger_plugin.html
[schedule description]: schedule.html
