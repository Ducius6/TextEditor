import Action.EditAction;
import Observers.StackObserver;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class UndoManager {

    private static UndoManager undoManager;

    private Stack<EditAction> undoStack = new Stack<>();
    private Stack<EditAction> redoStack = new Stack<>();

    List<StackObserver> stackObserverList = new ArrayList<>();

    private UndoManager() {
    }

    public void undo() {
        try {
            EditAction undoAction = undoManager.undoStack.pop();
            undoManager.redoStack.push(undoAction);
            undoAction.execute_undo();
            undoManager.notifyRedoStackObservers();
        } catch (EmptyStackException ignored) {
        }
    }

    public void redo() {
        try {
            EditAction redoAction = undoManager.redoStack.pop();
            redoAction.execute_do();
            undoManager.undoStack.push(redoAction);
            undoManager.notifyUndoStackObservers();
        } catch (EmptyStackException ignored) {
        }
    }

    public void push(EditAction editAction) {
        undoManager.redoStack.clear();
        undoManager.undoStack.push(editAction);
        undoManager.notifyRedoStackObservers();
        undoManager.notifyUndoStackObservers();
    }

    public static UndoManager getInstance() {
        if (undoManager != null) {
            return undoManager;
        }
        undoManager = new UndoManager();
        return undoManager;
    }

    public void addStackObserver(StackObserver observer) {
        undoManager.stackObserverList.add(observer);
    }

    public void removeStackObserver(StackObserver observer) {
        undoManager.stackObserverList.remove(observer);
    }

    private void notifyUndoStackObservers() {
        for (StackObserver observer : undoManager.stackObserverList) {
            observer.updateUndoStack();
        }
    }

    private void notifyRedoStackObservers() {
        for (StackObserver observer : undoManager.stackObserverList) {
            observer.updateRedoStack();
        }
    }

    public Boolean isRedoStackEmpty() {
        return undoManager.redoStack.isEmpty();
    }

    public Boolean isUndoStackEmpty() {
        return undoManager.undoStack.isEmpty();
    }
}
