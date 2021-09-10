import javax.swing.*;

public class PluginStatistics implements Plugin {

    @Override
    public String getName() {
        return "Statistics";
    }

    @Override
    public String getDescription() {
        return "Counts number of rows, words and letters in document";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {
        int numOfLines = model.getLines().size();
        int numOfWords = 0;
        int numOfLetters = 0;
        for (String line : model.getLines()) {
            String[] words = line.split(" ");
            numOfWords += words.length;
            for (String word : words) {
                numOfLetters += word.length();
            }
        }
        String message = "Statistics of file are = Lines: " + numOfLines + ", Words: " + numOfWords + ", Letters:" + numOfLetters;
        JOptionPane.showMessageDialog(new JFrame(), message);
    }
}
