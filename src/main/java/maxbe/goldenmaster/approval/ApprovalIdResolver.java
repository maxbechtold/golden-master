package maxbe.goldenmaster.approval;

import org.junit.jupiter.api.extension.ExtensionContext;

import maxbe.goldenmaster.junit.extension.GoldenMasterRun;

public class ApprovalIdResolver {

    private final GoldenMasterRun goldenMasterRunAnnotation;

    public ApprovalIdResolver(GoldenMasterRun goldenMasterRunAnnotation) {
        this.goldenMasterRunAnnotation = goldenMasterRunAnnotation;
    }

    public String resolveApprovalIdFor(ExtensionContext context) {
        return getApprovalId(context);

    }

    private String getApprovalId(ExtensionContext context) {
        if (!GoldenMasterRun.AUTO_ID.equals(goldenMasterRunAnnotation.id())) {
            return goldenMasterRunAnnotation.id();
        }
        return context.getRequiredTestMethod().getName();
    }

    public String resolveRunIdFor(ExtensionContext context) {
        return getApprovalId(context) + getRunIdSuffix(context.getDisplayName());
    }

    private String getRunIdSuffix(String displayName) {
        // Sadly, it's infeasible to provide the invocation index via the ExtensionContext as we get here
        // @BeforeEach test invocation. Thus, derive it from the display name.
        // TODO MAX Perhaps ExtensionContext#getConfigurationParameter can provide the index?
        String idWithoutBraces = displayName.substring(1, displayName.length() - 1);
        int runId = Integer.valueOf(idWithoutBraces) - 1;
        return "[" + runId + "]";
    }

}
