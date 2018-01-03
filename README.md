# golden-master
A harness for writing [Golden Master tests](https://dzone.com/articles/testing-legacy-code-golden) in Java

[![](https://jitpack.io/v/maxbechtold/golden-master.svg)](https://jitpack.io/#maxbechtold/golden-master) [![Build Status](https://travis-ci.org/maxbechtold/golden-master.svg?branch=master)](https://travis-ci.org/maxbechtold/golden-master)

## Requirements
Java 8

## How to use (preliminary explanation)

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

When you run your test for the first time, the outputs will serve as the initial *master files*. You are asked to run the test again to approve the previous outputs.

Every successive test run will then be matched against those master files - and fail if there are changes (e.g. due to mistakes during refactoring of the tested code).

If you change the tested code in a manner you consider *valid*, you can approve any changes that result in the output files by running the provided approval script.
