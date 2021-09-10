import Action.EditAction;

import java.util.ArrayList;
import java.util.List;

public class DeleteBeforeAction implements EditAction {
    private TextEditorModel textEditorModel;
    private List<String> pastLines;
    private Location pastCursorLocation;
    private LocationRange pastLocationRange;
    private Location location;
    private LocationRange locationRange;

    public DeleteBeforeAction(TextEditorModel textEditorModel) {
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
        pastCursorLocation = new Location(location.getX(), location.getY());

        if (textEditorModel.getSelectionRange() != null)
            pastLocationRange = new LocationRange(textEditorModel.getSelectionRange().getStart(), textEditorModel.getSelectionRange().getEnd());

        if (location.getX() == 0) {
            if (location.getY() != 0) {
                Location oldCursorLocation = new Location(location.getX(), location.getY());
                textEditorModel.moveCursorLeft();
                String line = textEditorModel.getLines().get(oldCursorLocation.getY());
                String newLine = textEditorModel.getLines().get(oldCursorLocation.getY() - 1) + line;
                textEditorModel.getLines().remove(oldCursorLocation.getY());
                textEditorModel.getLines().set(oldCursorLocation.getY() - 1, newLine);
            }
        } else {
            StringBuilder stringBuilder = new StringBuilder(textEditorModel.getLines().get(location.getY()));
            stringBuilder.deleteCharAt(location.getX() - 1);
            textEditorModel.getLines().set(location.getY(), stringBuilder.toString());
            textEditorModel.moveCursorLeft();
        }
        textEditorModel.notifyTextObservers();
    }

    @Override
    public void execute_undo() {
        textEditorModel.setLines(pastLines);
        textEditorModel.setCursorLocation(pastCursorLocation);
        textEditorModel.setSelectionRange(pastLocationRange);
        textEditorModel.notifyTextObservers();
    }
}
