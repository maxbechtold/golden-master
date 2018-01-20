package maxbe.goldenmaster.approval;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import maxbe.goldenmaster.junit.extension.GoldenMasterRun;

// TODO MAX Probably not meaningful anymore
@Disabled
public class TemplatedTestPathMapperTest {

    private ExtensionContext context;

    @BeforeEach
    void setUp() {
        context = Mockito.mock(ExtensionContext.class);
        Mockito.doReturn(getClass()).when(context).getRequiredTestClass();
        Mockito.doReturn(TemplatedTestPathMapperTest.class.getMethods()[0]).when(context).getRequiredTestMethod();
        Mockito.doReturn("[123]").when(context).getDisplayName();
    }

    @Test
    void usesTestIdIfSpecified() throws Exception {
        String testId = "test-id";
        Path tempDir = new File("root").toPath();
        TemplatedTestPathMapper<Object> mapper = new TemplatedTestPathMapper<>(context, tempDir, null);

        Path approvalFilePath = new File("file").toPath();
        Path approvalPath = mapper.getPath(new Object(), approvalFilePath);
        String separator = File.separator;
        String classPath = getClass().getName().replace('.', File.separatorChar);

        assertThat(approvalPath.toString()).isEqualTo(tempDir.toString() + separator + classPath + separator + "test-id"
                + separator + approvalFilePath.toString());
    }

    @Test
    void usesDisplayNameWithIndexWithoutId() throws Exception {
        Path tempDir = new File("root").toPath();
        TemplatedTestPathMapper<Object> mapper = new TemplatedTestPathMapper<>(context, tempDir, null);

        Path approvalFilePath = new File("file").toPath();
        Path approvalPath = mapper.getPath(new Object(), approvalFilePath);
        String separator = File.separator;
        String classPath = getClass().getName().replace('.', File.separatorChar);
        String basePath = tempDir.toString() + separator + classPath + separator;
        String testSpec = context.getRequiredTestMethod().getName() + "[122]"; // index = JUnit execution - 1;

        assertThat(approvalPath.toString()).isEqualTo(basePath + testSpec + separator + approvalFilePath.toString());
    }

    private GoldenMasterRun goldenMasterRun(String testId) {
        return new GoldenMasterRun() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return GoldenMasterRun.class;
            }

            @Override
            public int repetitions() {
                return GoldenMasterRun.DEFAULT_REPETITIONS;
            }

            @Override
            public String id() {
                return testId;
            }
        };
    }
}
