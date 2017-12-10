package maxbe.goldenmaster.junit.extension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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

import maxbe.goldenmaster.approval.ApprovalScriptWriter;
import maxbe.goldenmaster.approval.FileConverter;
import maxbe.goldenmaster.approval.JUnitReporter;
import maxbe.goldenmaster.approval.OS;
import maxbe.goldenmaster.approval.TemplatedTestPathMapper;

public class RunInvocationContextProvider implements TestTemplateInvocationContextProvider, BeforeAllCallback,
        BeforeEachCallback, AfterTestExecutionCallback, AfterAllCallback {

    private static final Namespace NAMESPACE = Namespace.create(RunInvocationContextProvider.class);
    private static final String REPORTER_KEY = "REPORTER";
    private static final String SCRIPT_WRITER_KEY = "SCRIPT_WRITER";
    private static final String APPROVAL_SCRIPT_NAME = "approveAllFailed";

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
        ApprovalScriptWriter approvalScriptWriter = createApprovalScriptWriter();
        getStore(context).put(SCRIPT_WRITER_KEY, approvalScriptWriter);
        getStore(context).put(REPORTER_KEY, new JUnitReporter(approvalScriptWriter));
    }

    private ApprovalScriptWriter createApprovalScriptWriter() {
        return new ApprovalScriptWriter(guessOperatingSystem(), getApprovalScriptFileName());
    }

    private File getApprovalScriptFileName() {
        if (guessOperatingSystem() == OS.Windows) {
            return new File(APPROVAL_SCRIPT_NAME + ".bat");
        }
        return new File(APPROVAL_SCRIPT_NAME);
    }

    private OS guessOperatingSystem() {
        return System.getProperty("os.name").toLowerCase().contains("win") ? OS.Windows : OS.ShellBased;
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
                .withReporter(get(context, JUnitReporter.class, REPORTER_KEY)).build();

        String fileName = context.getRequiredTestMethod().getName() + ".approved";
        approval.verify(outputFile, Paths.get(fileName));
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        ApprovalScriptWriter scriptWriter = get(context, ApprovalScriptWriter.class, SCRIPT_WRITER_KEY);
        boolean scriptCreated = scriptWriter.updateScript();

        if (scriptCreated) {
            System.out.println("Not all approvals passed, please execute " + getApprovalScriptFileName()
                    + " to approve current results");
        }
        outputFile.delete();
    }

    private <T> T get(ExtensionContext context, Class<T> type, String key) {
        return getStore(context).get(key, type);
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}
