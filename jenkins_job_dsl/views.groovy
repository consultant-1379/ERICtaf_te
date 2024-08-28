/*
Full API documentation:
https://jenkinsci.github.io/job-dsl-plugin/

Job DSL playground:
http://job-dsl.herokuapp.com/
*/

def executeInContext(Object context, Closure closure) {
    closure.delegate = context
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call()
}

def commonColumns(Object context) {
    executeInContext(context, {
        columns {
            status()
            weather()
            name()
            lastDuration()
            lastSuccess()
            lastFailure()
            lastBuildConsole()
            buildButton()
            configureProject()
        }
    })
}

listView('TE') {
    description('Jobs for TAF TE based on Jenkins 2.x')
    jobs {
        regex(/^TE-.*$/)
    }
    jobFilters {
        status {
            matchType(MatchType.EXCLUDE_MATCHED)
            status(Status.DISABLED)
        }
    }
    commonColumns(delegate)
}

listView('TAF Trigger') {
    description('Jobs for TAF trigger')
    jobs {
        regex(/^TAF_Trigger.*$/)
    }
    jobFilters {
        status {
            matchType(MatchType.EXCLUDE_MATCHED)
            status(Status.DISABLED)
        }
    }
    commonColumns(delegate)
}
