package maxbe.goldenmaster.junit.extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
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
        outputFile = File.createTempFile("goldenmaster_recording_" + System.currentTimeMillis(), ".txt");
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

    // TODO #3 Ensure same number of repetitions for all tests with the same approvalId by moving repetitions to @GoldenMasterTest?
    private int determineRepetitions(ExtensionContext context) {
        GoldenMasterRun goldenMasterAnnotation = getAnnotation(context);
        if (goldenMasterAnnotation == null) {
            return GoldenMasterRun.DEFAULT_REPETITIONS;
        }
        int repetitions = goldenMasterAnnotation.repetitions();
        if (repetitions < 1) {
            throw new IllegalArgumentException("Must specify a number greater than 0 for repetitions");
        }
        return repetitions;
    }

	private GoldenMasterRun getAnnotation(ExtensionContext context) {
		return context.getElement().get().getAnnotation(GoldenMasterRun.class);
	}

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ApprovalScriptWriter approvalScriptWriter = ApprovalScriptWriter.create(APPROVAL_SCRIPT_NAME);
        getStore(context).put(SCRIPT_WRITER_KEY, approvalScriptWriter);
        getStore(context).put(REPORTER_KEY, new JUnitReporter(approvalScriptWriter));
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
		String approvalId = getApprovalId(context) + getRunIdSuffix(context.getDisplayName());
		// TODO MAX Path must include run ID
		pathMapper = new TemplatedTestPathMapper<>(context, Paths.get("src", "test", "resources", "approved"), approvalId);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Approval<File> approval = Approval.of(File.class)//
                .withPathMapper(pathMapper)//
                // TODO #3 Fork and suggest fix
                .withConveter(new FileConverter())//
                .withReporter(get(context, JUnitReporter.class, REPORTER_KEY)).build();

        String fileName = getApprovalId(context) + ".approved";
        approval.verify(outputFile, Paths.get(fileName));
    }

	private String getApprovalId(ExtensionContext context) {
		GoldenMasterRun annotation = getAnnotation(context);
		if (!GoldenMasterRun.AUTO_ID.equals(annotation.id())) {
			return annotation.id();
		}
		return context.getRequiredTestMethod().getName();
	}

	private String getRunIdSuffix(String displayName) {
		// REVIEW #3 Can this be done better?
		String idWithoutBraces = displayName.substring(1, displayName.length() - 1);
		int runId = Integer.valueOf(idWithoutBraces) - 1;
		return "[" + runId + "]";
	}

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        ApprovalScriptWriter scriptWriter = get(context, ApprovalScriptWriter.class, SCRIPT_WRITER_KEY);
        boolean scriptCreated = scriptWriter.updateScript();

        if (scriptCreated) {
            System.out.println("Not all approvals passed, please execute " + scriptWriter.getScriptFile().getName()
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
