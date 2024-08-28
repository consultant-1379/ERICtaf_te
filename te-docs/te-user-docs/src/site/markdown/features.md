<head>
    <title>Features</title>
</head>

# Features

* TE provides a flexible and powerful schedule format that defines the order and sequence of execution, as well as parallel and sequential control for your tests.
* TE makes your testware not bound to any local test properties: it runs your testware with the properties set in CI portal and in the RFA job.
* Using TE will guarantee the particular sequence of test suites - read more [here](schedule.html).
* TE functionality is exposed to the end user as a build step for Jenkins that you can embed in your RFA job and use passed variables from Logic Script or other build steps.
* TE reuses Jenkins CI best part: ability to distribute work and orchestrate it.
* TE provides integration with CI portal.
* TE provides top-notch reporting based on one of the best open-source products: Allure reports. Report is embedded into Jenkins build's summary page.
* TE sends the test results to Event Repository and Visualization Engine.
* Each schedule item (set of suites) is executed in a separate JVM, which guarantees the isolation of each set of test suites.
* test-pom no longer needed.
