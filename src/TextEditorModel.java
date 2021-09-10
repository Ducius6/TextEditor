import Action.EditAction;
import Observers.CursorObserver;
import Observers.SelectionObserver;
import Observers.TextObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TextEditorModel {
    private List<String> lines;
    private LocationRange selectionRange;
    private Location cursorLocation;
    List<CursorObserver> cursorObserverList = new ArrayList<>();
    List<TextObserver> textObserverList = new ArrayList<>();
    List<SelectionObserver> selectionObserversList = new ArrayList<>();

    public TextEditorModel(String text) {
        setLines(new ArrayList<>(Arrays.asList(text.split("\n"))));
        setCursorLocation(new Location(0, 0));
    }

    public Iterator<String> allLines() {
        return lines.iterator();
    }

    public Iterator<String> linesRange(int index1, int index2) {
        if (index1 < 0 || index1 > lines.size()) index1 = 0;
        if (index2 < 1 || index2 > lines.size()) index2 = lines.size();

        return lines.subList(index1, index2).iterator();
    }

    public void addCursorObserver(CursorObserver cursorObserver) {
        this.cursorObserverList.add(cursorObserver);
    }

    public void removeCursorObserver(CursorObserver cursorObserver) {
        this.cursorObserverList.remove(cursorObserver);
    }

    public void addTextObserver(TextObserver textObserver) {
        this.textObserverList.add(textObserver);
    }

    public void removeTextObserver(TextObserver textObserver) {
        this.textObserverList.remove(textObserver);
    }

    public void moveCursorLeft() {
        if (cursorLocation.getX() != 0) {
            setCursorX(cursorLocation.getX() - 1);
        } else if (cursorLocation.getY() != 0) {
            setCursorY(cursorLocation.getY() - 1);
            setCursorX(lines.get(cursorLocation.getY()).length());
        }
        notifyCursorObservers();
    }

    public void moveCursorRight() {
        if (cursorLocation.getX() != lines.get(cursorLocation.getY()).length()) {
            setCursorX(cursorLocation.getX() + 1);
        } else if (cursorLocation.getY() != lines.size() - 1) {
            setCursorY(cursorLocation.getY() + 1);
            setCursorX(0);
        }
        notifyCursorObservers();
    }

    public void moveCursorUp() {
        if (getCursorLocation().getY() != 0) {
            if (getLines().get(getCursorLocation().getY() - 1).length() < getCursorLocation().getX()) {
                setCursorX(getLines().get(getCursorLocation().getY() - 1).length());
            }
            setCursorY(getCursorLocation().getY() - 1);
        } else {
            return;
        }
        notifyCursorObservers();
    }

    public void moveCursorDown() {
        if (getCursorLocation().getY() != getLines().size() - 1) {
            if (getLines().get(getCursorLocation().getY() + 1).length() < getCursorLocation().getX()) {
                setCursorX(getLines().get(getCursorLocation().getY() + 1).length());
            }
            setCursorY(getCursorLocation().getY() + 1);
        } else {
            return;
        }
        notifyCursorObservers();
    }

    public void deleteBefore() {
        EditAction editAction = new DeleteBeforeAction(this);
        editAction.execute_do();
        UndoManager.getInstance().push(editAction);
        notifyTextObservers();
    }

    public void deleteAfter() {
        EditAction editAction = new DeleteAfterAction(this);
        editAction.execute_do();
        UndoManager.getInstance().push(editAction);
        notifyTextObservers();
    }

    public void deleteRange(LocationRange locationRange) {
        EditAction editAction = new DeleteRangeAction(this);
        editAction.execute_do();
        UndoManager.getInstance().push(editAction);
        notifyTextObservers();
    }

    public void moveCursorToStart() {
        setCursorLocation(new Location(0, 0));
    }

    public void moveCursorToEnd() {
        setCursorLocation(new Location(lines.get(lines.size() - 1).length(), lines.size() - 1));
    }

    public LocationRange getSelectionRange() {
        return selectionRange;
    }

    public void setSelectionRange(LocationRange locationRange) {
        this.selectionRange = locationRange;
        notifySelectionObservers();
    }

    public void insert(char c) {
        EditAction editAction = new InsertTextAction(this, c);
        editAction.execute_do();
        UndoManager.getInstance().push(editAction);
        notifyTextObservers();
    }

    public void insert(String text) {
        EditAction editAction = new InsertTextAction(this, text);
        editAction.execute_do();
        UndoManager.getInstance().push(editAction);
        notifyTextObservers();
    }

    public void pressEnter() {
        EditAction editAction = new EnterAction(this);
        editAction.execute_do();
        UndoManager.getInstance().push(editAction);
        notifyTextObservers();
    }

    public String getSelectedText(LocationRange selectionRange) {
        if (selectionRange.getStart().getY() == selectionRange.getEnd().getY()) {
            return getLines().get(selectionRange.getStart().getY()).substring(selectionRange.getStart().getX(), selectionRange.getEnd().getX());
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getLines().get(selectionRange.getStart().getY()).substring(selectionRange.getStart().getX())).append("\n");
            for (int i = selectionRange.getStart().getY() + 1; i < selectionRange.getEnd().getY(); i++)
                stringBuilder.append(getLines().get(i)).append("\n");
            stringBuilder.append(getLines().get(selectionRange.getEnd().getY()), 0, selectionRange.getEnd().getX());
            return stringBuilder.toString();
        }
    }

    public void clearDocument() {
        LocationRange locationRange = new LocationRange(new Location(0, 0), new Location(lines.get(lines.size() - 1).length(), lines.size() - 1));
        setSelectionRange(locationRange);
        deleteRange(locationRange);
        setCursorLocation(new Location(0, 0));
        setSelectionRange(null);
    }

    public void replaceText(String text) {
        moveCursorToStart();
        setSelectionRange(null);
        setLines(new ArrayList<>(Arrays.asList(text.split("\n"))));
    }

    public String getText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getSelectedTextAndDelete(LocationRange selectionRange) {
        String line = getSelectedText(selectionRange);
        deleteRange(selectionRange);
        return line;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
        notifyTextObservers();
    }

    public Location getCursorLocation() {
        return cursorLocation;
    }

    public void setCursorX(int x) {
        cursorLocation.setX(x);
        notifyCursorObservers();
    }

    public void setCursorY(int y) {
        cursorLocation.setY(y);
        notifyCursorObservers();
    }

    public void setCursorLocation(Location cursorLocation) {
        this.cursorLocation = cursorLocation;
        notifyCursorObservers();
    }

    public void addSelectionObserver(SelectionObserver observer) {
        selectionObserversList.add(observer);
    }

    public void removeSelectionObserver(SelectionObserver observer) {
        selectionObserversList.remove(observer);
    }

    public void notifyTextObservers() {
        for (TextObserver textObserver : textObserverList) {
            textObserver.updateText();
        }
    }

    private void notifyCursorObservers() {
        for (CursorObserver observer : cursorObserverList) {
            observer.updateCursorLocation();
        }
    }

    private void notifySelectionObservers() {
        for (SelectionObserver observer : selectionObserversList) {
            observer.updateSelection();
        }
    }
}