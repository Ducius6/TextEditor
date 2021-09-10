import Action.EditAction;

import java.util.ArrayList;
import java.util.List;

public class DeleteRangeAction implements EditAction {
    private TextEditorModel textEditorModel;
    private List<String> pastLines;
    private Location pastCursorLocation;
    private LocationRange pastLocationRange;
    private Location location;
    private LocationRange locationRange;
    private List<String> lines;

    public DeleteRangeAction(TextEditorModel textEditorModel) {
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

        if (lines == null) {
            lines = textEditorModel.getLines();
        }

        pastLines = new ArrayList<>(textEditorModel.getLines());
        pastCursorLocation = new Location(location.getX(), location.getY());

        if (textEditorModel.getSelectionRange() != null)
            pastLocationRange = new LocationRange(textEditorModel.getSelectionRange().getStart(), textEditorModel.getSelectionRange().getEnd());

        if (locationRange.getStart().getY() == locationRange.getEnd().getY()) {
            StringBuilder line = new StringBuilder(textEditorModel.getLines().get(locationRange.getStart().getY()));
            line.delete(locationRange.getStart().getX(), locationRange.getEnd().getX());
            textEditorModel.getLines().set(locationRange.getStart().getY(), line.toString());
        } else {
            StringBuilder startLine = new StringBuilder(textEditorModel.getLines().get(locationRange.getStart().getY()));
            startLine.delete(locationRange.getStart().getX(), textEditorModel.getLines().get(locationRange.getStart().getY()).length());
            textEditorModel.getLines().set(locationRange.getStart().getY(), startLine.toString());

            StringBuilder endLine = new StringBuilder(textEditorModel.getLines().get(locationRange.getEnd().getY()));
            endLine.delete(0, locationRange.getEnd().getX());

            textEditorModel.getLines().set(locationRange.getStart().getY(), startLine.toString() + endLine.toString());
            textEditorModel.getLines().remove(locationRange.getEnd().getY());

            List<String> toRemove = new ArrayList<>();
            for (int i = locationRange.getStart().getY() + 1; i < locationRange.getEnd().getY(); i++) {
                toRemove.add(textEditorModel.getLines().get(i));
            }
            textEditorModel.getLines().removeAll(toRemove);
        }
        textEditorModel.getCursorLocation().setX(locationRange.getStart().getX());
        textEditorModel.getCursorLocation().setY(locationRange.getStart().getY());
        textEditorModel.setSelectionRange(null);
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
