package app;

import javafx.application.Application;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Main is a JavaFX Application, so these tests only check its structure and do
 * not start the JavaFX toolkit (no window is opened).
 */
public class MainTest {

    @Test
    public void testMain_isJavaFxApplication() {
        assertTrue(Application.class.isAssignableFrom(Main.class));
    }

    @Test
    public void testMain_canBeInstantiatedWithoutToolkit() {
        assertNotNull(new Main());
    }

    @Test
    public void testMain_hasPublicStaticMainMethod() throws NoSuchMethodException {
        Method main = Main.class.getMethod("main", String[].class);
        assertTrue(Modifier.isStatic(main.getModifiers()));
        assertEquals(void.class, main.getReturnType());
    }

    @Test
    public void testMain_overridesStart() throws NoSuchMethodException {
        Method start = Main.class.getDeclaredMethod("start", javafx.stage.Stage.class);
        assertEquals(Main.class, start.getDeclaringClass());
    }
}
