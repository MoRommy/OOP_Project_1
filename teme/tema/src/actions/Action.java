package actions;

import fileio.ActionInputData;
import fileio.Input;

public final class Action {

    private Action() {

    }

    /**
     * Switches the action and run it
     * @param input database
     * @param action action
     * @return result message
     */
    public static String act(final Input input, final ActionInputData action) {
        return switch (action.getActionType()) {
            case "command" -> Commands.act(input, action);
            case "query" -> Queries.act(input, action);
            case "recommendation" -> Recommendations.act(input, action);
            default -> "Invalid action!";
        };
    }
}
