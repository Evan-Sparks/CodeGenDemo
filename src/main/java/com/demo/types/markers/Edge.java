package com.demo.types.markers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to mark a getter or setter as being associated with an edge to another data object rather than a value.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Edge {
}
