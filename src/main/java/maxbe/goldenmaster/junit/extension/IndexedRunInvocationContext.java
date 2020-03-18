package maxbe.goldenmaster.junit.extension;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

class IndexedRunInvocationContext implements TestTemplateInvocationContext {

    private final Integer index;
    private final File outputFile;
    private final RunInvocationContextProvider contextProvider;

    IndexedRunInvocationContext(Integer index, File outputFile, RunInvocationContextProvider contextProvider) {
        this.index = index;
        this.outputFile = outputFile;
        this.contextProvider = contextProvider;
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
        return Arrays.asList(indexResolver(), outputFileResolver(), contextProviderResolver());
    }

    private ParameterResolver indexResolver() {
        return new ParameterResolver() {

            @Override
            public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                return parameterContext.getParameter().getType().equals(Integer.class);
            }

            @Override
            public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                return index;
            }
        };
    }

    private ParameterResolver outputFileResolver() {
        return new ParameterResolver() {

            @Override
            public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                return parameterContext.getParameter().getType().equals(File.class);
            }

            @Override
            public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                return outputFile;
            }
        };
    }

    private ParameterResolver contextProviderResolver() {
        return new ParameterResolver() {

            @Override
            public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                return parameterContext.getParameter().getType().equals(RunInvocationContextProvider.class);
            }

            @Override
            public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                return contextProvider;
            }
        };
    }
}