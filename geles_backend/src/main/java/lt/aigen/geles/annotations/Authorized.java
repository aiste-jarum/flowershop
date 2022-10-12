package lt.aigen.geles.annotations;


import org.springframework.web.bind.annotation.RequestAttribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorized {
     boolean admin() default false;
     boolean optional() default false; //Cannot access Optional<Type> through reflection due to type erasure
}

