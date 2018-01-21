package maxbe.goldenmaster.approval;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

public class TemplatedTestPathMapperTest {

    private ExtensionContext context;

    @BeforeEach
    void setUp() {
        context = Mockito.mock(ExtensionContext.class);
        Mockito.doReturn(getClass()).when(context).getRequiredTestClass();
    }

    @Test
    void mapsPackageStructureToPath() throws Exception {
        String approvalId = "test-id";
        Path tempDir = new File("root").toPath();
        TemplatedTestPathMapper<Object> mapper = new TemplatedTestPathMapper<>(context, tempDir, approvalId);

        Path approvalFilePath = new File("file").toPath();
        Path approvalPath = mapper.getPath(new Object(), approvalFilePath);

        String separator = File.separator;
        String classPath = getClass().getName().replace('.', File.separatorChar);

        assertThat(approvalPath.toString()) //
                .isEqualTo(tempDir.toString() + separator //
                        + classPath + separator //
                        + approvalId + separator //
                        + approvalFilePath.toString());
    }

}
