package maxbe.goldenmaster.approval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.github.approval.converters.Converter;

public class FileConverter implements Converter<File> {

	@Override
	public byte[] getRawForm(File value) {
		try {
			return Files.readAllBytes(value.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
