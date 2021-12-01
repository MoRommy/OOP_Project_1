package actions;

import actions.command.Command;
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
        switch (action.getActionType()) {
            case "command":
                return Command.act(input, action);

            case "query":

                break;

            case "recomandations:":

                break;
            default:
        }
        return "Invalid action!";
    }
}
