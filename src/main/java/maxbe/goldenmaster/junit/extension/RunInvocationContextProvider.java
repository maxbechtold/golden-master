package maxbe.goldenmaster.junit.extension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.github.approval.Approval;

import maxbe.goldenmaster.approval.FileConverter;
import maxbe.goldenmaster.approval.JUnitReporter;
import maxbe.goldenmaster.approval.TemplatedTestPathMapper;

public class RunInvocationContextProvider implements TestTemplateInvocationContextProvider, BeforeAllCallback,
        BeforeEachCallback, AfterTestExecutionCallback, AfterAllCallback {

    private static final Namespace NAMESPACE = Namespace.create(RunInvocationContextProvider.class);
    private static final String REPORTER_KEY = "REPORTER";
    private static final String APPROVAL_BAT_FILE_NAME = "approveAllFailed.bat";

    private final File outputFile;

    private TemplatedTestPathMapper<File> pathMapper;

    public RunInvocationContextProvider() throws IOException {
        outputFile = File.createTempFile("goldenmaster_recording_" + System.currentTimeMillis(), "txt");
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        int repetitions = determineRepetitions(context);
        return IntStream.range(0, repetitions).boxed().map(index -> new IndexedRunInvocationContext(index, outputFile));
    }

    private int determineRepetitions(ExtensionContext context) {
        GoldenMasterRun goldenMasterAnnotation = context.getElement().get().getAnnotation(GoldenMasterRun.class);
        if (goldenMasterAnnotation == null) {
            return GoldenMasterRun.DEFAULT_REPETITIONS;
        }
        int repetitions = goldenMasterAnnotation.repetitions();
        if (repetitions < 1) {
            throw new IllegalArgumentException("Must specify a number greater than 0 for repetitions");
        }
        return repetitions;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getStore(context).put(REPORTER_KEY, new JUnitReporter());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        pathMapper = new TemplatedTestPathMapper<>(context, Paths.get("src", "test", "resources", "approved"));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Approval<File> approval = Approval.of(File.class)//
                .withPathMapper(pathMapper)//
                // TODO #3 Fork and suggest fix
                .withConveter(new FileConverter())//
                .withReporter(getReporter(context)).build();

        String fileName = context.getRequiredTestMethod().getName() + ".approved";
        approval.verify(outputFile, Paths.get(fileName));
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        List<String> commands = getReporter(context).getApprovalCommands();
        File approvalFile = new File(APPROVAL_BAT_FILE_NAME);
        if (commands.isEmpty()) {
            approvalFile.delete();
        } else {
            System.out.println(
                    "Not all approvals passed, please run " + APPROVAL_BAT_FILE_NAME + " to approve current results");
            Files.write(approvalFile.toPath(), commands, StandardCharsets.UTF_8);
        }
        outputFile.delete();
    }

    private JUnitReporter getReporter(ExtensionContext context) {
        return getStore(context).get(REPORTER_KEY, JUnitReporter.class);
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}
