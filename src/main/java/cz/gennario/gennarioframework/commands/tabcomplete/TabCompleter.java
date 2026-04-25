package cz.gennario.gennarioframework.commands.tabcomplete;

import java.util.List;

/**
 * Functional interface for dynamic tab completion.
 */
@FunctionalInterface
public interface TabCompleter {

    List<String> complete(TabContext context);

}
