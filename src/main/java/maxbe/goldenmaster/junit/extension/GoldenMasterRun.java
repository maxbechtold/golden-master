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

    /**
     * A positive number to specify a custom amount of repetitions for each run.
     */
    int repetitions() default DEFAULT_REPETITIONS;

    /**
     * Allows to define multiple instrumentations of the same usecase which will be approved against each other.
     * <p/>
     *
     * One use case is to test saving/loading the program state where you write two test methods that 1) run the program
     * start to end and 2) run the program with intermittent saving and loading. If you expect the outcome to be
     * identical, you can approve 2) against 1) by using the same <i>approval ID.</i>
     * <p/>
     *
     * It is up to you to ensure that runs with the same approval ID have the same number of <code>repetitions</code>.
     *
     */
    String id() default AUTO_ID;

}
