import Action.EditAction;

import java.util.ArrayList;
import java.util.List;

public class DeleteAfterAction implements EditAction {
    private TextEditorModel textEditorModel;
    private List<String> pastLines;
    private Location pastCursorLocation;
    private LocationRange pastLocationRange;
    private Location location;
    private LocationRange locationRange;

    public DeleteAfterAction(TextEditorModel textEditorModel) {
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

        if (location.getX() == textEditorModel.getLines().get(location.getY()).length()) {
            if (location.getY() != textEditorModel.getLines().size() - 1) {
                String line = textEditorModel.getLines().get(location.getY() + 1);
                String newLine = textEditorModel.getLines().get(location.getY()) + line;
                textEditorModel.getLines().remove(location.getY() + 1);
                textEditorModel.getLines().set(location.getY(), newLine);
            }
        } else {
            StringBuilder stringBuilder = new StringBuilder(textEditorModel.getLines().get(location.getY()));
            stringBuilder.deleteCharAt(location.getX());
            textEditorModel.getLines().set(location.getY(), stringBuilder.toString());
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
