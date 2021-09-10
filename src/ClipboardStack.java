import Observers.ClipboardObserver;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class ClipboardStack {
    Stack<String> texts = new Stack<>();
    List<ClipboardObserver> clipboardObserverList = new ArrayList<>();

    public void push(String string) {
        texts.push(string);
        notifyClipboardObservers();
    }

    public String pop() {
        try {
            String text = texts.pop();
            notifyClipboardObservers();
            return text;
        } catch (EmptyStackException ignored) { }
        notifyClipboardObservers();
        return "";
    }

    public String peek() {
        try {
            return texts.peek();
        } catch (EmptyStackException ignored) { }
        return "";
    }

    public Boolean isEmpty() {
        return texts.isEmpty();
    }

    public void clear() {
        texts.clear();
    }

    public void addClipboardObserver(ClipboardObserver clipboardObserver) {
        clipboardObserverList.add(clipboardObserver);
    }

    public void removeClipboardObserver(ClipboardObserver clipboardObserver) {
        clipboardObserverList.remove(clipboardObserver);
    }

    public void notifyClipboardObservers() {
        for (ClipboardObserver clipboardObserver : clipboardObserverList) clipboardObserver.updateClipboard();
    }
}
