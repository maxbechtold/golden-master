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
    public static final String AUTO_ID = "";

    int repetitions() default DEFAULT_REPETITIONS;
    
    /**
     * Allows to define multiple instrumentations of the same usecase which can be approved against each other.
     * TODO #3 Document in GitHub
     */
    String id() default AUTO_ID;

}
