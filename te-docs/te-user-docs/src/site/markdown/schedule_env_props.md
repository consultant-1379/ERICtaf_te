<head>
    <title>Environmental Properties</title>
</head>

# Environmental Properties

It is possible to define different types of environmental properties for the test execution. These properties allow to customize
different test execution aspects like JRE version or JVM settings. Also, it's possible to define system properties that
will be passed to TAF runtime.

To define environmental properties you have to define *env-properties* tags for a schedule, item-group or for an individual item.
Each *env-properties* tag has the following required child tag:

* *&lt;property&gt;* - the environmental property that you wish to set.

Additionally, the following attributes are required:

* *type* - currently supported types are "jvm" and "system";
* *key* - property name, depends on type.

## JVM settings

JVM settings is environmental properties subtype that allow you to customize Java version and JVM settings like heap size, GC tuning, etc.
The type of JVM environmental property is "jvm".

### Custom Java version

To customize Java version (it's Java 7 by default) you should define a `property` tag with attribute `key` equal to `version`
and value equal to the JVM version that is supported by TE environment (7 or 8).

Example:

```
<env-properties>
    <property type="jvm" key="version">8</property>
</env-properties>
```

### JVM options

Basically it's all that you can normally define in `JAVA_OPTS`. Define heap size, GC tuning, etc.

To do it you should define a `property` tag with attribute `key` equal to `options` and appropriate value.

Example:

```
<env-properties>
    <property type="jvm" key="options">-verbose:class -Xms3G -Xmx3G</property>
</env-properties>
```

### Max thread count (experimental)

This option lets you to limit the max number of threads the test JVM can have. If this limit is reached, thread dump will be created and
 attached to the test report, and testware won't be able to create any new threads.

Example:

```
<env-properties>
    <property type="jvm" key="maxThreads">55</property>
</env-properties>
```

Please note that this option is currently experimental, and can be removed at any stage without any notice.

## System properties

System properties will be passed as runtime properties (normally passed via `-D`) to TAF runtime.

Each system property is a `property` tag with type `system` attribute. `key` attribute and value should depict the
actual system property name and value.

A good example of system property is a TAF profile definition. Other properties defined here should have a valid excuse for this.

System property definitions should not be used to pass host properties and ordinary test properties, to avoid the use of schedule
as a property file.

System property definition example:

```
<env-properties>
    <property type="system" key="taf.profiles">aProfile</property>
</env-properties>
```

Some properties will be ignored to prevent TAF improper behaviour:

* groups
* suites
* taf.http_config.url
* suitethreadpoolsize
* testng.test.classpath
* dir
* fakeSuitePath

**Please note** that system properties will override the properties calculated and passed from trigger job - according
to [TAF configuration layers hierarchy](https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/snapshot/taf_concepts/taf-configuration.html).

## Hierarchy

Environmental properties can be set for a whole schedule, for an `item-group` or for an individual `item`,
and multiple environmental properties can be set for different items, or item-groups.

The definition at the lower level overrides the definition at the higher level.
<br/>
So you can can define some shared properties (like TAF profile) at the schedule level, and override them as needed on lower levels.

**Note: If setting an environmental variable on an item, it must be specified as the last attribute inside the item.**

## Advanced schedule with multiple environmental properties example

```
<schedule>
    <!-- Environmental Properties for all schedule items -->
    <env-properties>
        <property type="jvm" key="version">8</property>
        <property type="system" key="taf.profiles">defaultProfile</property>
    </env-properties>
    <item>
        <name>Preparation step</name>
        <component>com.groupId:preparation</component>
        <suites>1.xml</suites>
        <groups>installation</groups>

        <!-- Environmental Property for particular item -->
        <env-properties>
            <property type="jvm" key="version">7</property>
        </env-properties>
    </item>

    <item-group parallel="true">
        <!-- Environmental property that will be shared by all items in this item-group -->
        <env-properties>
            <property type="system" key="taf.profiles">aProfile</property>
        </env-properties>
        <item timeout-in-seconds="60">
            <name>Parallel 1</name>
            <component>com.groupId:artifactId</component>
            <suites>2.xml</suites>
        </item>
        <item stop-on-fail="true">
            <name>Parallel 2</name>
            <component>com.groupId:artifactId</component>
            <suites>3.xml</suites>
            <env-properties>
                <property type="jvm" key="options">-Xms3G -Xmx3G</property>
                <property type="system" key="taf.profiles">bProfile</property>
            </env-properties>
        </item>
    </item-group>

    <item>
        <name>Finalization step</name>
        <component>com.groupId:preparation</component>
        <suites>2.xml</suites>
        <groups>acceptance,cleanup</groups>
    </item>
</schedule>
```

In this example the following items will have appropriate settings:

* *Preparation step* - Java 7, TAF profile = 'defaultProfile'
* *Parallel 1* - Java 8, TAF profile = 'aProfile'
* *Parallel 2* - Java 8, TAF profile = 'bProfile', JVM settings '-Xms3G -Xmx3G' apply

## Included schedules

The included schedule (defined via `<include ...>`) inherits the property definitions from the external schedule
and can override them, but included schedule's definitions are encapsulated inside it and not exposed to siblings or external schedule.
<br/>
*Please note* that if included schedule includes yet another schedule, the same rules will apply.
