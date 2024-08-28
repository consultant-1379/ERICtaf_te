<head>
    <title>Quickest Run</title>
</head>

# Quickest run possible

TAF TE provides the possibility to distribute your tests into independent chunks, separating them into different
schedule items. However, if you have a few suites that can be run in parallel, but do not need separation,
you can put all those suites in one schedule item:

```
<item>
    <name>Fast test</name>
    <component>com.groupId:artifactId</component>
    <suites>short.xml,medium.xml,long.xml</suites>
</item>
```

This will run faster than

```
<item>
    <name>Fast test 1</name>
    <component>com.groupId:artifactId</component>
    <suites>short.xml</suites>
</item>
<item>
    <name>Fast test 2</name>
    <component>com.groupId:artifactId</component>
    <suites>medium.xml</suites>
</item>
<item>
    <name>Fast test 3</name>
    <component>com.groupId:artifactId</component>
    <suites>long.xml</suites>
</item>
```

Why? Because each item in TE is run by a separate Maven process, which takes time to download artifacts from Nexus, etc.
Running few suites within one item process will save time here.

However, this approach needs to be balanced - running too many
tests run in parallel in one JVM cause a risk of running out of memory, especially if you have memory leaks.
