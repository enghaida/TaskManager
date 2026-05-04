package taskmanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class whose behavior depends on live weather data.
 * Any component annotated with {@code @WeatherSensitive} may produce
 * different results based on current weather conditions and should be
 * tested with both clear and rainy weather scenarios.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface WeatherSensitive {
    /**
     * Optional description of how weather affects this component.
     *
     * @return a short description of the weather dependency
     */
    String value() default "";
}
