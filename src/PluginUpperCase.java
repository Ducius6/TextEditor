public class PluginUpperCase implements Plugin {
    @Override
    public String getName() {
        return "Upper case";
    }

    @Override
    public String getDescription() {
        return "Capitalize every word in file";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {
        for (int i = 0; i < model.getLines().size(); i++) {
            model.getLines().set(i, upperCaseAllFirst(model.getLines().get(i)));
        }
        model.notifyTextObservers();
    }

    private static String upperCaseAllFirst(String value) {
        char[] array = value.toCharArray();

        array[0] = Character.toUpperCase(array[0]);
        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }
        return new String(array);
    }
}