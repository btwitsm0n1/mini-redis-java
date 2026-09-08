public class CommandParser {

    public String[] parse(String input) {

        if (input == null || input.trim().isEmpty()) {
            return new String[0];
        }

        return input.trim().split("\\s+");
    }
}