package org.jenkinsci.plugins.weibo.token;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import net.java.sezpoz.Indexable;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

@Indexable(
        type = TokenMacro.class)
@Retention(SOURCE)
@Target({ TYPE })
@Documented
public @interface WeiboToken {
    /* we don't need anything in here, just the annotation itself */
}