package maxbe.goldenmaster.example;

import static java.util.Collections.singleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import maxbe.goldenmaster.junit.extension.GoldenMasterRun;
import maxbe.goldenmaster.junit.extension.GoldenMasterTest;

@EnabledIfSystemProperty(named = "maxbe.goldenmaster.testWithExamples", matches = "true")
@GoldenMasterTest
public class ExampleGoldenMasterTest {

    private File outputFile;

    /**
     * Set up your Golden Master run. The parameter {@code index} is just for illustration - drop it if you don't need
     * it
     *
     */
    @BeforeEach
    void setUp(File outputFile, Integer index) throws Exception {
        this.outputFile = outputFile;
    }

    /**
     * Instrument your code/program for the provided index. You can specify a number of {@code repetitions} if you're
     * unhappy with the default value.
     * <p/>
     * Also, you can provide an {@code id} that bundles instrumentation variations together. See
     * {@link GoldenMasterRun#id()}.
     */
    @GoldenMasterRun(repetitions = 5)
    void test(Integer index) throws IOException {
        int inputValue = index * 1000;

        String outputValue = new AwkwardClass().doYourMagic(inputValue);

        Files.write(outputFile.toPath(), singleton(outputValue));
    }

}
