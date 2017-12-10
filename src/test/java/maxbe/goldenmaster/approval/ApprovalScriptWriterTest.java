package maxbe.goldenmaster.approval;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

public class ApprovalScriptWriterTest {

    private File sourceFile = new File("source");
    private File targetFile = new File("target");

    @Test
    void writesSingleMoveCommand() throws Exception {
        File scriptFile = File.createTempFile("script", ".bat");
        ApprovalScriptWriter approvalScriptWriter = new ApprovalScriptWriter(OS.Windows, scriptFile);

        approvalScriptWriter.addMoveCommand(sourceFile, targetFile);
        approvalScriptWriter.updateScript();

        assertThat(scriptFile.exists()).isTrue();
        assertThat(scriptFile).hasContent(
                "move /Y \"" + sourceFile.getAbsolutePath() + "\" \"" + targetFile.getAbsolutePath() + "\"");
    }

    @Test
    void writesSeveralMoveCommands() throws Exception {
        File scriptFile = File.createTempFile("script", ".bat");
        ApprovalScriptWriter approvalScriptWriter = new ApprovalScriptWriter(OS.Windows, scriptFile);

        approvalScriptWriter.addMoveCommand(sourceFile, targetFile);
        approvalScriptWriter.addMoveCommand(targetFile, sourceFile);
        approvalScriptWriter.updateScript();

        assertThat(scriptFile.exists()).isTrue();
        assertThat(scriptFile)
                .hasContent("move /Y \"" + sourceFile.getAbsolutePath() + "\" \"" + targetFile.getAbsolutePath() + "\"" //
                        + System.lineSeparator() //
                        + "move /Y \"" + targetFile.getAbsolutePath() + "\" \"" + sourceFile.getAbsolutePath() + "\"");
    }

    @Test
    void writesLinuxCommands() throws Exception {
        File scriptFile = File.createTempFile("script", "");
        ApprovalScriptWriter approvalScriptWriter = new ApprovalScriptWriter(OS.ShellBased, scriptFile);

        approvalScriptWriter.addMoveCommand(sourceFile, targetFile);
        approvalScriptWriter.updateScript();

        assertThat(scriptFile.exists()).isTrue();
        assertThat(scriptFile)
                .hasContent("mv -f \"" + sourceFile.getAbsolutePath() + "\" \"" + targetFile.getAbsolutePath() + "\"");
    }

    @Test
    void deletesScriptIfNoCommandsGiven() throws Exception {
        File scriptFile = File.createTempFile("script", "");
        assertThat(scriptFile.exists()).isTrue();

        ApprovalScriptWriter approvalScriptWriter = new ApprovalScriptWriter(OS.ShellBased, scriptFile);
        approvalScriptWriter.updateScript();

        assertThat(scriptFile.exists()).isFalse();
    }
}
