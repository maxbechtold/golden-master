package maxbe.goldenmaster.approval;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.ExtensionContext;

import com.github.approval.PathMapper;

// FIXME MAX There's a bootstrap problem, the initial approval files are empty...
public class TemplatedTestPathMapper<T> implements PathMapper<T> {

	private final Path approvalPath;

	public TemplatedTestPathMapper(ExtensionContext context, Path basePath, String approvalId) {
		Class<?> testClass = context.getRequiredTestClass();

		approvalPath = basePath.resolve(testClass.getName().replace(".", File.separator)).resolve(approvalId);
	}

	@Override
	public Path getPath(T value, Path approvalFilePath) {
		return approvalPath.resolve(approvalFilePath);
	}

}
