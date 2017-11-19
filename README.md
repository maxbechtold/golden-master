# golden-master
A harness for writing Golden Master tests in Java

## How to use (preliminary explanation)
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
