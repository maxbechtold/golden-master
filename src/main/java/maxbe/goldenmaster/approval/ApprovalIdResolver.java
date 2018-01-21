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
        // REVIEW #3 Can this be done better?
        String idWithoutBraces = displayName.substring(1, displayName.length() - 1);
        int runId = Integer.valueOf(idWithoutBraces) - 1;
        return "[" + runId + "]";
    }

}
