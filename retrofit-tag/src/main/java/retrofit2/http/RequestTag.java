package retrofit2.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by xian on 2017/11/17.
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface RequestTag {
}
