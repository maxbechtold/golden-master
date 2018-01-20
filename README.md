# golden-master
A harness for easy writing of [Golden Master tests](https://dzone.com/articles/testing-legacy-code-golden) in Java. Follow the JitPack button below in order to include it in your project.

[![](https://jitpack.io/v/maxbechtold/golden-master.svg)](https://jitpack.io/#maxbechtold/golden-master) [![Build Status](https://travis-ci.org/maxbechtold/golden-master.svg?branch=master)](https://travis-ci.org/maxbechtold/golden-master)

## Benefits

* No cumbersome boilerplate code - focus on your program instrumentation
* Built-in repetitions - easily verify 10, 100, 10,000 program inputs
* IDE integration - continuous visual feedback
![screenshot](https://github.com/maxbechtold/golden-master/blob/master/src/main/resources/docs/ide-support.PNG)

## How to use (basic explanation)

You basically implement a [JUnit 5 templated test](http://junit.org/junit5/docs/current/user-guide/#writing-tests-test-templates) like so (check out ``ExampleGoldenMasterTest`` for a more detailed example):

```java
@GoldenMasterTest
public class ATest {

  @BeforeEach
  void setUp(File outputFile, Integer index) throws Exception {
    // Setup up the inputs for your test run which must write output to the given file
  }
  
  @GoldenMasterRun
  void instrumentProgramUnderTest(Integer index) throws Exception {
    // Do some hard work whose output will be compared to that of previous runs
  }
}
```

When you run your test for the first time, the outputs will serve as the initial *master files*. You are asked to *approve* the previous outputs by running the tests again and then manually executing a generated script file*.

Every successive test run will then be matched against those master files - and fail if there are changes (e.g. due to mistakes during refactoring of the tested code).

If you change the tested code in a manner you consider *valid*, you can approve any changes that result in the output files by running the provided approval script.

*(I'm working on simplifying this step)

## Requirements
Java 8
