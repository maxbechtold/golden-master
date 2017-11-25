package maxbe.goldenmaster.approval;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.ExtensionContext;

import com.github.approval.PathMapper;

public class TemplatedTestPathMapper<T> implements PathMapper<T> {

    private final Path currentTestPath;

    public TemplatedTestPathMapper(ExtensionContext context, Path path) {
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        String runIdSuffix = getRunIdSuffix(context.getDisplayName());

        currentTestPath = path.resolve(testClass.getName().replace(".", File.separator))
                .resolve(testMethod.getName() + runIdSuffix);
    }

    private String getRunIdSuffix(String displayName) {
        // TODO #35 This has to be done better!
        int runId = Integer.valueOf(displayName.substring(1, displayName.length() - 1)) - 1;
        String runIdSuffix = "[" + runId + "]";
        return runIdSuffix;
    }

    @Override
    public Path getPath(T value, Path approvalFilePath) {
        return currentTestPath.resolve(approvalFilePath);
    }

}
