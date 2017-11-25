package maxbe.goldenmaster.example;

import static java.util.Collections.singleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;

import maxbe.goldenmaster.junit.extension.GoldenMasterRun;
import maxbe.goldenmaster.junit.extension.GoldenMasterTest;

@GoldenMasterTest
public class ExampleGoldenMasterTest {

    private File outputFile;

    @BeforeEach
    void setUp(File outputFile, Integer index) throws Exception {
        this.outputFile = outputFile;
    }

    @GoldenMasterRun(repetitions = 5)
    void test(Integer index) throws IOException {
        int inputValue = index * 1000;

        String outputValue = new AwkwardClass().doYourMagic(inputValue);

        Files.write(outputFile.toPath(), singleton(outputValue));
    }

}
