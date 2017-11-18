package maxbe.goldenmaster.approval;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.AssumptionViolatedException;
import org.opentest4j.AssertionFailedError;

import com.github.approval.Reporter;

public class JUnitReporter implements Reporter {

	private static final double MAX_DEVIATION = 0.000;
	private List<String> approvalCommands = new LinkedList<>();

	public List<String> getApprovalCommands() {
		return approvalCommands;
	}

	@Override
	public void notTheSame(byte[] oldValue, File fileForVerification, byte[] newValue, File fileForApproval) {
		if (oldValue.length == newValue.length) {
			double error = calculateError(oldValue, newValue);
			if (error < MAX_DEVIATION) {
				throw new AssumptionViolatedException(String.format("Approval failed with less than %s %% difference, skipping test", MAX_DEVIATION * 100));
			}
		}

		addMoveCommand(fileForVerification, fileForApproval);
		throw new AssertionFailedError("Approval failed, please check console output.\n", new String(oldValue, UTF_8), new String(newValue, UTF_8));
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

	private void addMoveCommand(File fileForVerification, File fileForApproval) {
		approvalCommands.add(getMoveCommand(fileForVerification, fileForApproval));
	}

	// TODO #35 Get rid of OS dependency
	private String getMoveCommand(File fileForVerification, File fileForApproval) {
		return "move /Y " + quote(fileForApproval) + " " + quote(fileForVerification);
	}

	private String quote(File file) {
		return "\"" + file.getAbsolutePath() + "\"";
	}

	@Override
	public boolean canApprove(File fileForApproval) {
		return true;
	}

	@Override
	public void approveNew(byte[] value, File fileForApproval, File fileForVerification) {
		fail("First approval, created approval file. Please run again");
	}
}