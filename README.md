# Java Tracing Agent
A lightweight and fast runtime injection tool for logging and tracing that can also trace OSGi applications.

Inject logging anywhere into your running application.

## Setup
- add VM Argument with path to the tracing jar `-javaagent:dakaraphi.devtools.tracing-all.jar`
- add sytem property for path to tracing config `-Ddakaraphi.devtools.tracing.config.file=tracer.json`

## How does it work?
Java Tracing Agent uses the Java Instrumentation support included in the JVM to modify existing bytecode of classes.
As classes are loaded, they are modified prior to execution.
Classes can also be modified at runtime if we change the tracing configuration.

## Features
- Runtime updates
  - Changes can be made while the application is up and running
  - Any changes to the tracing configuration JSON are picked up and applied at runtime
  - All tracers can be turned on or off at runtime
- Logging
  - Log method parameters, return value and instance variables
  - Log thread ID and/or thread name
- Conditional Logging
  - Log based on variable or parameter value
  - Log based on stack frame classes and methods
  - Log based on current thread name
- Selection of classes and methods for log injection
  - Specify classes and methods as regular expressions
  - Specify location of injection can be begin/end of method or any line number within
- Stack Frames Logging
  - Log the class, method and line number
  - Log only frames matching regular expression pattern
  - Log limited number of frames
- Log Format
  - Single line or multiline
  - Optionally include thread name and/or thread id

## Config Definitions
Variable expressions than can be used in the `variables` section

*variables values:*
- `$0, $1, $2, ...` *this* and actual parameters
- `$args` An array of parameters. The type of $args is Object[]
- `$_` The resulting value
- `localVariable` Local variables can be used simply by name

Tracer definition
```javascript
{
    "tracers": [
        {
            "enabled": "true",    // optional. default 'true'. 
            "name": "tracerName",  // optional. name will be logged in output
            "classRegex": ".*classname", "methodRegex": "methodname", "line": "0",  // specify the location for the log injection
            "variables": [ // optional. specify variables to include in the output
              {"name":"logAsName", // optional. name to use when logging
              "expression": "localVarName"} // optional. value to log
            ],
            "logWhen": { // optional. specify conditions for when to log
                "stackFramesRegex": "package.*classname",  // optional. log when any stack frame class and method matches
                "variableValues": [
                    {"name": "logAsName", "valueRegex": ".*value.*"}  // optional. log when a logging variable matches this value
                ],                
                "threadNameRegex": ".*threadname.*"  // optional. log when thread name matches
            },
            "logStackFrames": { // optional. log class, method and line number from elements of the stack trace
                "limit": 10, // optional. log max number of frames
                "includeRegex": "package.*",  // optional. log only frames matching
                "excludeRegex": "package.*",  // optional. exclude frames from logging
                "referenceDuplicatesByHash": "false"  // optional. Do not log the same full stack frames if already logged.  Log the hash id of the original instead.
            }
        }
    ],
    "logConfig": {
      "threadName": "false", // optional. include thread name
	    "threadId": "false", // optional. include thread Id
	    "executionCount": "false", // optional. include the count of executions for each tracer
	    "multiLine": "false" // optional. log variable values on new lines
    }
}
 ```

A simple tracer definition JSON
```javascript
{
    "tracers": [   
        {
            "name": "Request",
            "classRegex": "com.example.MyClass", "methodRegex": "myMethod", "line": "0",
            "variables": ["$1"]
        },
    ]
}
```

## Supporting libraries
These are libraries that make Java Tracing Agent possible
- [Javassist](https://github.com/jboss-javassist/javassist) used to assist rewrite of the bytecodes for classes
- [Jackson](https://github.com/FasterXML/jackson) used to load the JSON configuration

## Build
`gradlew shadowJar` will create jar file `dakaraphi.devtools.tracing/build/libs/dakaraphi.devtools.tracing-all.jar`