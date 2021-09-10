import Action.EditAction;

import java.util.ArrayList;
import java.util.List;

public class EnterAction implements EditAction {
    private TextEditorModel textEditorModel;
    private List<String> pastLines;
    private Location pastCursorLocation;
    private LocationRange pastLocationRange;
    private Location location;
    private LocationRange locationRange;

    public EnterAction(TextEditorModel textEditorModel) {
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

        if (locationRange != null) {
            textEditorModel.deleteRange(locationRange);
            location = new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY());
        }
        String firstSplittedLine = textEditorModel.getLines().get(location.getY()).substring(0, location.getX());
        String secondSplittedLine = textEditorModel.getLines().get(location.getY()).substring(location.getX());
        textEditorModel.getLines().set(location.getY(), firstSplittedLine);
        textEditorModel.getLines().add(location.getY() + 1, secondSplittedLine);
        textEditorModel.getCursorLocation().setY(location.getY() + 1);
        textEditorModel.getCursorLocation().setX(0);
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
