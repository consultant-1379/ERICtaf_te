<head>
    <title>Test Execution Schedule</title>
</head>

# Test execution schedule

## Introduction

TAF TE schedule is an XML file that defines the execution of RFA/KGB test suite set.

It provides the following benefits:

* ability to run the test suites (or even groups of test suites) in parallel
* define if schedule item execution failure should stop overall execution
* define the max execution time for the schedule item

## Structure description

TAF Executor (TE) Schedule is used to describe the flow of test suite execution. Basic example of schedule is:

``` xml
<?xml version="1.0"?>
<schedule xmlns="http://taf.lmera.ericsson.se/schema/te"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://taf.lmera.ericsson.se/schema/te http://taf.lmera.ericsson.se/schema/te/schedule/xml">
    <item>
        <name>name1</name>
        <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>
        <suites>suite1.xml, suite2.xml</suites>
    </item>
</schedule>
```

Schedules consist of items, item groups and includes. Schedule item represents one or more TAF test suites.

For information on schedule management, click [here](schedule_source.html)

<br/>

### Items

Main component of schedule is test item. Each item has following required attributes:

* *&lt;name&gt;* - name of test item.  Should be unique in scope of schedule.
* *&lt;component&gt;* - name of group:artifact of testware in Nexus (e.g., *com.ericsson.cifwk.taf.executor:te-taf-testware*). Optionally, it can have version as well (e.g., com.ericsson.cifwk.taf.executor:te-taf-testware:1.2.3). Version allows to reference suites from different versions of the same testware artifact.
* *&lt;suites&gt;* - comma separated list of TestNG test suites. Maximum length 255 characters.

Additionally item may have following optional attributes:

* *stop-on-fail* - (true/false) should the tests continue if one schedule item fails? False by default.
* *timeout-in-seconds* - timeout after which test execution process will be terminated if it returns no result. No timeout by default.
* *groups* - TestNG test groups.

<br/>

### Guaranteed sequence of suites

Putting few sequential items with one suite each will guarantee the sequential execution of these suites,
which you cannot do in TestNG:

```
<item>
    <name>Suite 1</name>
    <component>com.groupId:artifactId</component>
    <suites>first.xml</suites>
</item>
<item>
    <name>Suite 2</name>
    <component>com.groupId:artifactId</component>
    <suites>second.xml</suites>
</item>
```

<br/>

### Time-limited tests

```
<item timeout-in-seconds="60">
    <name>Flaky test</name>
    <component>com.groupId:artifactId</component>
    <suites>short.xml, long.xml</suites>
</item>
```

If you have some flaky tests that are likely to hang, or the tests that must pass in a limited amount of time,
you can set a timeout using *timeout-in-seconds* attribute in the appropriate &lt;item&gt; tag. If the tests will
take longer than this value, the test process will be aborted. The overall test run will be marked as failed.

<br/>

### Parallel execution. Item groups

It is possible to combine multiple items in item groups. Tag &lt;item-group&gt;. Item group has an optional boolean parameter *parallel* which allows test suite parallel execution (false by default).

Item groups may also contain child item groups:

```
<item-group parallel="true">
    <item>
        ...
    </item>
    <item>
        ...
    </item>
    <item-group>
        <item>
            ...
        </item>
        ...
    </item-group>
</item-group>
```

<br/>

### Includes

It is possible to include one schedule into another (can include only schedules contained in the same Maven artifact!):

```
<schedule>
    <item>
        ...
    </item>
    <include>group:artifact/path/to/schedule_for_inclusion.xml</include>
</schedule>
```

<br/>

### Advanced schedule example

```
<?xml version="1.0"?>
<schedule xmlns="http://taf.lmera.ericsson.se/schema/te"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://taf.lmera.ericsson.se/schema/te http://taf.lmera.ericsson.se/schema/te/schedule/xml">

    <item>
        <name>Preparation step</name>
        <component>com.groupId:preparation</component>
        <suites>1.xml</suites>
        <groups>installation</groups>
    </item>

    <item-group parallel="true">
        <item timeout-in-seconds="60">
            <name>Parallel 1</name>
            <component>com.groupId:artifactId</component>
            <suites>2.xml</suites>
        </item>
        <item stop-on-fail="true">
            <name>Parallel 2</name>
            <component>com.groupId:artifactId</component>
            <suites>3.xml</suites>
        </item>
        <item>
            <name>Parallel 3</name>
            <component>com.groupId2:artifactId2</component>
            <suites>4.xml,5.xml,6.xml</suites>
        </item>
    </item-group>

    <item>
        <name>Finalization step</name>
        <component>com.groupId:preparation</component>
        <suites>1.xml</suites>
        <groups>acceptance,cleanup</groups>
    </item>

</schedule>
```

In this example suites 2.xml, 3.xml, 4.xml, 5.xml, 6.xml are run in parallel. After all of them are finished, suite 1.xml is executed for test groups 'acceptance' and 'cleanup'.

<br/>

### Item groups executed in parallel

Yet another example:

```
<schedule xmlns="http://taf.lmera.ericsson.se/schema/te"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://taf.lmera.ericsson.se/schema/te http://taf.lmera.ericsson.seschema/te/schedule/xml">

    <item-group parallel="true">
        <item-group parallel="false">
            <item>
                <name>Step 11</name>
                <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>
                <suites>one.xml</suites>
            </item>
            <item>
                <name>Step 12</name>
                <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>
                <suites>two.xml</suites>
            </item>
        </item-group>
        <item-group parallel="false">
            <item>
                <name>Step 21</name>
                <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>
                <suites>three.xml</suites>
            </item>
        </item-group>
    </item-group>
</schedule>
```

In this case two item groups are run in parallel - suites one.xml and three.xml will run in parallel. Suite two.xml will be run when one.xml is finished.
