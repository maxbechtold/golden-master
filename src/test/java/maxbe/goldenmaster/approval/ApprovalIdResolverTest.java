package maxbe.goldenmaster.approval;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import maxbe.goldenmaster.junit.extension.GoldenMasterRun;

public class ApprovalIdResolverTest {

    private ExtensionContext context;

    @BeforeEach
    void setUp() {
        context = Mockito.mock(ExtensionContext.class);
        Mockito.doReturn(someMethod()).when(context).getRequiredTestMethod();
        Mockito.doReturn("[123]").when(context).getDisplayName();
    }

    @Test
    void noExceptionIfNoAnnotationPresent() throws Exception {
        ApprovalIdResolver resolver = new ApprovalIdResolver(null);

        String approvalId = resolver.resolveApprovalIdFor(context);

        assertThat(approvalId).isEqualTo(someMethod().getName());
    }

    @Test
    void resolvesIdIfSpecified() throws Exception {
        String testId = "test-id";

        ApprovalIdResolver resolver = new ApprovalIdResolver(goldenMasterRun(testId));

        String approvalId = resolver.resolveApprovalIdFor(context);

        assertThat(approvalId).isEqualTo(testId);
    }

    @Test
    void resolvesDisplayNameForAutoId() throws Exception {
        ApprovalIdResolver resolver = new ApprovalIdResolver(goldenMasterRun(GoldenMasterRun.AUTO_ID));

        assertThat(resolver.resolveApprovalIdFor(context)).isEqualTo(someMethod().getName());
    }

    @Test
    void resolvesRunIdBasedOnApprovalId() throws Exception {
        ApprovalIdResolver resolver = new ApprovalIdResolver(goldenMasterRun(GoldenMasterRun.AUTO_ID));

        String runId = resolver.resolveRunIdFor(context);

        assertThat(runId).isEqualTo(someMethod().getName() + "[122]"); // index = JUnit execution number - 1;
    }

    private Method someMethod() {
        return getClass().getMethods()[0];
    }

    private GoldenMasterRun goldenMasterRun(String customId) {
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
                return customId;
            }
        };
    }
}
