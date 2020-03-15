package maxbe.goldenmaster.junit.extension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.github.approval.Approval;
import com.github.approval.Reporter;
import com.github.approval.utils.ApprovalScriptWriter;

import maxbe.goldenmaster.approval.ApprovalIdResolver;
import maxbe.goldenmaster.approval.FileConverter;
import maxbe.goldenmaster.approval.JUnitReporter;
import maxbe.goldenmaster.approval.TemplatedTestPathMapper;

public class RunInvocationContextProvider
        implements TestTemplateInvocationContextProvider, BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    static final Namespace NAMESPACE = Namespace.create(RunInvocationContextProvider.class);

    private static final String REPORTER_KEY = "REPORTER";
    private static final String SCRIPT_WRITER_KEY = "SCRIPT_WRITER";
    private static final String APPROVAL_SCRIPT_NAME = "approve";

    private final File outputFile;

    private TemplatedTestPathMapper<File> pathMapper;

    public RunInvocationContextProvider() throws IOException {
        outputFile = File.createTempFile("goldenmaster_recording_" + System.currentTimeMillis(), ".txt");
    }

    // FIXME #11 Currently, the harness fails with multiple TestDescriptors, see warning when using @GoldenMasterRun
    // with @Test
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return getSupportedAnnotation(context) != null;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        int repetitions = determineRepetitions(context);
        return IntStream.range(0, repetitions).boxed().map(index -> new IndexedRunInvocationContext(index, outputFile));
    }

    private int determineRepetitions(ExtensionContext context) {
        GoldenMasterRun goldenMasterAnnotation = getSupportedAnnotation(context);
        if (goldenMasterAnnotation == null) {
            return GoldenMasterRun.DEFAULT_REPETITIONS;
        }
        int repetitions = goldenMasterAnnotation.repetitions();
        if (repetitions < 1) {
            throw new IllegalArgumentException("Must specify a number greater than 0 for repetitions");
        }
        return repetitions;
    }

    private GoldenMasterRun getSupportedAnnotation(ExtensionContext context) {
        return context.getElement().map(element -> element.getAnnotation(GoldenMasterRun.class)).orElse(null);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        String contextId = context.getTestClass().map(Class::getName).map(name -> "-" + name).orElse("");
        ApprovalScriptWriter approvalScriptWriter = ApprovalScriptWriter.create(APPROVAL_SCRIPT_NAME + contextId);

        getStore(context).put(SCRIPT_WRITER_KEY, approvalScriptWriter);
        getStore(context).put(REPORTER_KEY, new JUnitReporter(approvalScriptWriter));
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        String runId = new ApprovalIdResolver(getSupportedAnnotation(context)).resolveRunIdFor(context);
        Path basePath = Paths.get("src", "test", "resources", "approved");
        pathMapper = new TemplatedTestPathMapper<>(context, basePath, runId);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Approval<File> approval = Approval.of(File.class)//
                .withPathMapper(pathMapper)//
                .withConverter(new FileConverter())//
                .withReporter(get(context, Reporter.class, REPORTER_KEY)).build();

        ApprovalIdResolver approvalIdResolver = new ApprovalIdResolver(getSupportedAnnotation(context));

        String fileName = approvalIdResolver.resolveApprovalIdFor(context) + ".approved";
        approval.verify(outputFile, Paths.get(fileName));
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        ApprovalScriptWriter scriptWriter = get(context, ApprovalScriptWriter.class, SCRIPT_WRITER_KEY);
        boolean scriptCreated = scriptWriter.updateScript();

        if (scriptCreated) {
            System.out.println("Not all approvals passed, please execute " + scriptWriter.getScriptFile().getName() + " to approve current results");
        }
        outputFile.delete();
    }

    private <T> T get(ExtensionContext context, Class<T> type, String key) {
        T element = getStore(context).get(key, type);
        if (element == null) {
            throw new RuntimeException(
                    String.format("Did not find element for key '%s', did you annotate your class with @%s?", key, GoldenMasterTest.class.getSimpleName()));
        }
        return element;
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}
