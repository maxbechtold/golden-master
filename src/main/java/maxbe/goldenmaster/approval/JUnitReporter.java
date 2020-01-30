package maxbe.goldenmaster.approval;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import com.github.approval.Reporter;

// TODO #10 Move this and rest of package to approval library
public class JUnitReporter implements Reporter {

    private static final double MAX_DEVIATION = 0.000;
    private final ApprovalScriptWriter approvalScriptWriter;

    public JUnitReporter(ApprovalScriptWriter approvalScriptWriter) {
        this.approvalScriptWriter = approvalScriptWriter;
    }

    @Override
    public void notTheSame(byte[] oldValueBytes, File fileForVerification, byte[] newValueBytes, File fileForApproval) {
        if (oldValueBytes.length == newValueBytes.length) {
            double error = calculateError(oldValueBytes, newValueBytes);
            if (error < MAX_DEVIATION) {
                throw new TestAbortedException(String
                        .format("Approval failed with less than %s %% difference, skipping test", MAX_DEVIATION * 100));
            }
        }

        String message = "Approval failed, please check console output.\n";
        notifyMismatch(fileForVerification, fileForApproval, message, asString(oldValueBytes), asString(newValueBytes));
    }

    private void notifyMismatch(File fileForVerification, File fileForApproval, String message, String oldValue,
            String newValue) throws AssertionFailedError {
        approvalScriptWriter.addMoveCommand(fileForApproval, fileForVerification);
        AssertionFailedError error = new AssertionFailedError(message, oldValue, newValue);
        String format = "expected: %sactual:   %s%n";
        System.err.printf(format, error.getExpected().getStringRepresentation(), error.getActual().getStringRepresentation());
        throw error;
    }

    private String asString(byte[] oldValue) {
        return new String(oldValue, StandardCharsets.UTF_8);
    }

    private double calculateError(byte[] oldValue, byte[] newValue) {
        int length = oldValue.length;
        long diffCount = 0;
        for (int i = 0; i < newValue.length; i++) {
            if (oldValue[i] != newValue[i]) {
                diffCount++;
            }
        }
        return diffCount / (double) length;
    }

    @Override
    public boolean canApprove(File fileForApproval) {
        return true;
    }

    @Override
    public void approveNew(byte[] newValueBytes, File fileForApproval, File fileForVerification) {
        String message = "First approval, please check console output.\n";
        notifyMismatch(fileForVerification, fileForApproval, message, "", asString(newValueBytes));
    }
}