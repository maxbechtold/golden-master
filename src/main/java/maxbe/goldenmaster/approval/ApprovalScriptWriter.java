package maxbe.goldenmaster.approval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ApprovalScriptWriter {

    private final File scriptFile;
    private final String moveCommand;
    private final String overwriteFlag;

    private StringBuilder scriptBuilder;

    public ApprovalScriptWriter(OS os, File scriptFile) {
        this.scriptFile = scriptFile;
        scriptBuilder = new StringBuilder();
        moveCommand = determineMoveCommand(os);
        overwriteFlag = determineOverwriteFlag(os);
    }

    private String determineOverwriteFlag(OS os) {
        return os == OS.Windows ? "/Y" : "-f";
    }

    private String determineMoveCommand(OS os) {
        return os == OS.Windows ? "move" : "mv";
    }

    public void addMoveCommand(File sourceFile, File targetFile) {
        scriptBuilder.append(moveCommand);
        scriptBuilder.append(" ");
        scriptBuilder.append(overwriteFlag);
        scriptBuilder.append(" ");
        scriptBuilder.append(quote(sourceFile));
        scriptBuilder.append(" ");
        scriptBuilder.append(quote(targetFile));
        scriptBuilder.append(System.lineSeparator());
    }

    private String quote(File file) {
        return "\"" + file.getAbsolutePath() + "\"";
    }

    public boolean updateScript() {
        if (scriptBuilder.length() == 0) {
            scriptFile.delete();
            return false;
        }

        writeScriptContent();
        return true;
    }

    private void writeScriptContent() {
        try {
            Files.write(scriptFile.toPath(), scriptBuilder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
