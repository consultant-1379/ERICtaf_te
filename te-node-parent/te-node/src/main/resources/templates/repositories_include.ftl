<repositories>
    <repository>
        <releases>
            <enabled>true</enabled>
            <updatePolicy>daily</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
        </releases>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
            <checksumPolicy>fail</checksumPolicy>
        </snapshots>
        <id>repositoryId</id>
        <name>Repository Name</name>
        <url>${repositoryUrl}</url>
        <layout>default</layout>
    </repository>
</repositories>

<distributionManagement>
</distributionManagement>
