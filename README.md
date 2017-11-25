# golden-master
A harness for writing [Golden Master tests](https://dzone.com/articles/testing-legacy-code-golden) in Java

[![](https://jitpack.io/v/maxbechtold/golden-master.svg)](https://jitpack.io/#maxbechtold/golden-master)

## Requirements
Java 8

## How to use (preliminary explanation)

You basically implement a [JUnit 5 templated test](http://junit.org/junit5/docs/current/user-guide/#writing-tests-test-templates) like so:

```java
@BeforeEach
void setUp(File outputFile, Integer index) throws Exception {
  // Setup up your test inputs which must write output to the file given
}

@TestTemplate
@ExtendWith(RunInvocationContextProvider.class)
void instrumentProgramUnderTest(Integer index) throws Exception {
  // Do some hard work whose output will be compared to that of previous runs
}
```
