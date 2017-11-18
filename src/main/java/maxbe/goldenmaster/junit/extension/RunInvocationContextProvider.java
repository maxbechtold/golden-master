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
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.github.approval.Approval;

import maxbe.goldenmaster.approval.FileConverter;
import maxbe.goldenmaster.approval.JUnitReporter;
import maxbe.goldenmaster.approval.TemplatedTestPathMapper;

public class RunInvocationContextProvider implements TestTemplateInvocationContextProvider, BeforeEachCallback, AfterTestExecutionCallback, AfterAllCallback {

	private static final int RUNS = 100;
	// TODO #35 Other output means?
	private final File outputFile;

	private static final String APPROVAL_BAT_FILE_NAME = "approveAllFailed.bat";
	private static final JUnitReporter REPORTER = new JUnitReporter();

	public TemplatedTestPathMapper<File> pathMapper;

	public RunInvocationContextProvider() throws IOException {
		outputFile = File.createTempFile("goldenmaster_recording_" + System.currentTimeMillis(), "txt");
	}

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		return IntStream.range(0, RUNS).boxed().map(index -> new IndexedRunInvocationContext(index, outputFile));
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		pathMapper = new TemplatedTestPathMapper<>(context, Paths.get("src", "test", "resources", "approved"));
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		Approval<File> approval = Approval.of(File.class)//
				.withPathMapper(pathMapper)//
				// TODO #35 Fork and suggest fix
				.withConveter(new FileConverter())//
				.withReporter(REPORTER).build();

		String fileName = context.getRequiredTestMethod().getName() + ".approved";
		approval.verify(outputFile, Paths.get(fileName));
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		List<String> commands = REPORTER.getApprovalCommands();
		File approvalFile = new File(APPROVAL_BAT_FILE_NAME);
		if (commands.isEmpty()) {
			approvalFile.delete();
		} else {
			System.out.println("Not all approvals passed, please run " + APPROVAL_BAT_FILE_NAME + " to approve current results");
			Files.write(approvalFile.toPath(), commands, StandardCharsets.UTF_8);
		}
		outputFile.delete();
	}
}
