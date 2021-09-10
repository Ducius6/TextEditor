import Action.EditAction;

import java.util.ArrayList;
import java.util.List;

public class InsertTextAction implements EditAction {
    private TextEditorModel textEditorModel;
    private List<String> pastLines;
    private Location pastCursorLocation;
    private LocationRange pastLocationRange;
    private Location location;
    private LocationRange locationRange;

    char c;
    String string;

    public InsertTextAction(TextEditorModel textEditorModel, char c) {
        this.c = c;
        this.textEditorModel = textEditorModel;
    }

    public InsertTextAction(TextEditorModel textEditorModel, String string) {
        this.string = string;
        this.textEditorModel = textEditorModel;
    }

    @Override
    public void execute_do() {
        if (location == null)
            location = new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY());
        else {
            textEditorModel.setCursorLocation(new Location(location.getX(), location.getY()));
        }
        if (locationRange == null)
            if (textEditorModel.getSelectionRange() != null)
                locationRange = new LocationRange(textEditorModel.getSelectionRange().getStart(), textEditorModel.getSelectionRange().getEnd());

        pastLines = new ArrayList<>(textEditorModel.getLines());
        pastCursorLocation = new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY());

        if (textEditorModel.getSelectionRange() != null)
            pastLocationRange = new LocationRange(textEditorModel.getSelectionRange().getStart(), textEditorModel.getSelectionRange().getEnd());

        if (string != null) {
            if (locationRange != null) {
                textEditorModel.deleteRange(locationRange);
                location = new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY());
            }
            StringBuilder stringBuilder = new StringBuilder(textEditorModel.getLines().get(location.getY()));
            stringBuilder.insert(location.getX(), string);
            String[] splittedText = stringBuilder.toString().split("\n");
            textEditorModel.getLines().set(location.getY(), splittedText[0]);
            for (int i = 1; i < splittedText.length; i++) {
                textEditorModel.getLines().add(location.getY() + i, splittedText[i]);
            }
            for (int i = 0; i < string.length(); i++) textEditorModel.moveCursorRight();
        } else {
            if (locationRange != null) {
                location = new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY());
                textEditorModel.deleteRange(locationRange);
            }
            StringBuilder stringBuilder = new StringBuilder(textEditorModel.getLines().get(location.getY()));
            stringBuilder.insert(location.getX(), c);
            textEditorModel.getLines().set(location.getY(), stringBuilder.toString());
            textEditorModel.moveCursorRight();
        }
    }

    @Override
    public void execute_undo() {
        textEditorModel.setLines(pastLines);
        textEditorModel.setCursorLocation(pastCursorLocation);
        textEditorModel.setSelectionRange(pastLocationRange);
        textEditorModel.notifyTextObservers();
    }
}
