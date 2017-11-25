package maxbe.goldenmaster.junit.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RunInvocationContextProvider.class)
public @interface GoldenMasterRun {

    public static final int DEFAULT_REPETITIONS = 100;

    int repetitions() default DEFAULT_REPETITIONS;
}
