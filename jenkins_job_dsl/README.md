# Prerequisites
## Seed Job

In order to create all required jobs for TE, manually create a free-style job
with DSL plugin-powered build step *Process job DSLs* which then creates
the following seed job that automatically creates all defined TE jobs:

```
job('TE-job-seed') {
    description('Automatically created seed job for auto provisioning the rest of TE jobs')
    scm {
        git {
            remote {
                name('origin')
                url('${GERRIT_CENTRAL}/OSS/com.ericsson.cifwk/ERICtaf_te')
            }
            branch('master')
        }
    }
    triggers {
        scm 'H/2 * * * *'
    }
    steps {
        dsl {
            external 'jenkins_job_dsl/*.groovy'
        }
    }
}
```
