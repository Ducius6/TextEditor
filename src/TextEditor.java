import Observers.*;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextEditor extends JFrame {
    JMenuItem open = new JMenuItem("Open");
    JMenuItem save = new JMenuItem("Save");
    JMenuItem exit = new JMenuItem("Exit");
    JMenuItem undo = new JMenuItem("Undo");
    JMenuItem redo = new JMenuItem("Redo");
    JMenuItem cut = new JMenuItem("Cut");
    JMenuItem copy = new JMenuItem("Copy");
    JMenuItem paste = new JMenuItem("Paste");
    JMenuItem pasteAndTake = new JMenuItem("Paste and Take");
    JMenuItem deleteSelection = new JMenuItem("Delete selection");
    JMenuItem clearDocument = new JMenuItem("Clear document");
    JMenuItem cursorToStart = new JMenuItem("Cursor to document start");
    JMenuItem cursorToEnd = new JMenuItem("Cursor to document end");
    JMenu plugins = new JMenu("Plugins");
    JLabel infoLabel = new JLabel("");

    private static TextEditorModel textEditorModel;
    private static ClipboardStack clipboardStack;
    private int margins = 1;
    private int lineIndent = 20;
    private TextPanel textPanel;
    private Boolean isShiftPressed = false;
    private Boolean isControlPressed = false;
    private int shiftMoved = 0;
    private Location startLocationOfSelection;

    public TextEditor(String text) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(100, 50);
        setLayout(new BorderLayout());
        setSize(800, 600);
        setTitle("MainWindow");
        initTextEditorModel(text);
        clipboardStack = new ClipboardStack();
        initGUI();
        textEditorModel.addCursorObserver(() -> {
            textPanel.revalidate();
            textPanel.repaint();
            infoLabel.setText(putLabelText());
        });
        textEditorModel.addTextObserver(() -> {
            textPanel.revalidate();
            textPanel.repaint();
            infoLabel.setText(putLabelText());
        });
        textEditorModel.addSelectionObserver(() -> {
            if (textEditorModel.getSelectionRange() != null) {
                cut.setEnabled(true);
                copy.setEnabled(true);
                deleteSelection.setEnabled(true);
            } else {
                cut.setEnabled(false);
                copy.setEnabled(false);
                deleteSelection.setEnabled(false);
            }
        });
        clipboardStack.addClipboardObserver(() -> {
            if (clipboardStack.isEmpty()) {
                paste.setEnabled(false);
                pasteAndTake.setEnabled(false);
            } else {
                paste.setEnabled(true);
                pasteAndTake.setEnabled(true);
            }
        });
        UndoManager.getInstance().stackObserverList.add(new StackObserver() {
            @Override
            public void updateRedoStack() {
                if (UndoManager.getInstance().isRedoStackEmpty()) {
                    redo.setEnabled(false);
                } else {
                    redo.setEnabled(true);
                }
            }

            @Override
            public void updateUndoStack() {
                if (UndoManager.getInstance().isUndoStackEmpty()) {
                    undo.setEnabled(false);
                } else {
                    undo.setEnabled(true);
                }
            }
        });
    }

    private void initGUI() {
        setText();
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu move = new JMenu("Move");
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(move);
        menuBar.add(plugins);

        open = new JMenuItem("Open");
        save = new JMenuItem("Save");
        exit = new JMenuItem("Exit");
        file.add(open);
        file.add(save);
        file.add(exit);

        open.addActionListener(a -> {
            try {
                openFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        save.addActionListener(a -> {
            try {
                saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        exit.addActionListener(a -> System.exit(0));

        undo = new JMenuItem("Undo");
        redo = new JMenuItem("Redo");
        cut = new JMenuItem("Cut");
        copy = new JMenuItem("Copy");
        paste = new JMenuItem("Paste");
        pasteAndTake = new JMenuItem("Paste and Take");
        deleteSelection = new JMenuItem("Delete selection");
        clearDocument = new JMenuItem("Clear document");
        edit.add(undo);
        edit.add(redo);
        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        edit.add(pasteAndTake);
        edit.add(deleteSelection);
        edit.add(clearDocument);

        cut.setEnabled(false);
        copy.setEnabled(false);
        undo.setEnabled(false);
        redo.setEnabled(false);
        paste.setEnabled(false);
        deleteSelection.setEnabled(false);
        pasteAndTake.setEnabled(false);

        undo.addActionListener(a -> undoFunc());
        redo.addActionListener(a -> redoFunc());
        cut.addActionListener(a -> cutFunc());
        copy.addActionListener(a -> copyFunc());
        paste.addActionListener(a -> pasteFunc());
        pasteAndTake.addActionListener(a -> pasteAndTakeFunc());
        deleteSelection.addActionListener(a -> deleteSelectionFunc());
        clearDocument.addActionListener(a -> clearDocument());

        cursorToStart = new JMenuItem("Cursor to document start");
        cursorToEnd = new JMenuItem("Cursor to document end");
        move.add(cursorToStart);
        move.add(cursorToEnd);

        cursorToStart.addActionListener(a -> textEditorModel.moveCursorToStart());
        cursorToEnd.addActionListener(a -> textEditorModel.moveCursorToEnd());

        try {
            loadPlugins();
        } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException | InstantiationException ignored) {
        }

        this.setJMenuBar(menuBar);
    }

    private void setText() {
        Container container = getContentPane();
        textPanel = new TextPanel();
        container.add(textPanel, BorderLayout.CENTER);
        infoLabel.setText(putLabelText());
        infoLabel.setBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK));
        container.add(infoLabel, BorderLayout.SOUTH);
    }

    private void initTextEditorModel(String text) {
        textEditorModel = new TextEditorModel(text);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    int beforeLocationX = textEditorModel.getCursorLocation().getX();
                    int beforeLocationY = textEditorModel.getCursorLocation().getY();
                    textEditorModel.moveCursorLeft();
                    if (isShiftPressed) {
                        shiftMoved--;
                        if (startLocationOfSelection == null) {
                            startLocationOfSelection = new Location(beforeLocationX, beforeLocationY);
                        }
                        if (shiftMoved > 0) {
                            textEditorModel.setSelectionRange(new LocationRange(startLocationOfSelection, new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY())));
                        } else {
                            textEditorModel.setSelectionRange(new LocationRange(new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY()), startLocationOfSelection));
                        }
                    } else {
                        refreshVariables();
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    beforeLocationX = textEditorModel.getCursorLocation().getX();
                    beforeLocationY = textEditorModel.getCursorLocation().getY();
                    textEditorModel.moveCursorRight();
                    if (isShiftPressed) {
                        shiftMoved++;
                        if (startLocationOfSelection == null) {
                            startLocationOfSelection = new Location(beforeLocationX, beforeLocationY);
                        }
                        if (shiftMoved < 0) {
                            textEditorModel.setSelectionRange(new LocationRange(new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY()), startLocationOfSelection));

                        } else {
                            textEditorModel.setSelectionRange(new LocationRange(startLocationOfSelection, new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY())));
                        }
                    } else {
                        refreshVariables();
                    }
                    break;
                case KeyEvent.VK_UP:
                    beforeLocationX = textEditorModel.getCursorLocation().getX();
                    beforeLocationY = textEditorModel.getCursorLocation().getY();
                    textEditorModel.moveCursorUp();
                    if (isShiftPressed) {
                        int firstLine = textEditorModel.getLines().get(beforeLocationY).substring(0, beforeLocationX).length();
                        int secondLine = textEditorModel.getLines().get(textEditorModel.getCursorLocation().getY()).substring(textEditorModel.getCursorLocation().getX()).length();
                        shiftMoved -= (firstLine + secondLine);
                        if (startLocationOfSelection == null) {
                            startLocationOfSelection = new Location(beforeLocationX, beforeLocationY);
                        }
                        if (shiftMoved > 0) {
                            textEditorModel.setSelectionRange(new LocationRange(startLocationOfSelection, new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY())));
                        } else {
                            textEditorModel.setSelectionRange(new LocationRange(new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY()), startLocationOfSelection));
                        }
                    } else {
                        refreshVariables();
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    beforeLocationX = textEditorModel.getCursorLocation().getX();
                    beforeLocationY = textEditorModel.getCursorLocation().getY();
                    textEditorModel.moveCursorDown();
                    if (isShiftPressed) {
                        int firstLine = textEditorModel.getLines().get(beforeLocationY).substring(beforeLocationX).length();
                        int secondLine = textEditorModel.getLines().get(textEditorModel.getCursorLocation().getY()).substring(0, textEditorModel.getCursorLocation().getX()).length();
                        shiftMoved += firstLine + secondLine;
                        if (startLocationOfSelection == null) {
                            startLocationOfSelection = new Location(beforeLocationX, beforeLocationY);
                        }
                        if (shiftMoved < 0) {
                            textEditorModel.setSelectionRange(new LocationRange(new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY()), startLocationOfSelection));

                        } else {
                            textEditorModel.setSelectionRange(new LocationRange(startLocationOfSelection, new Location(textEditorModel.getCursorLocation().getX(), textEditorModel.getCursorLocation().getY())));
                        }
                    } else {
                        refreshVariables();
                    }
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    if (textEditorModel.getSelectionRange() != null) {
                        deleteSelectionFunc();
                    } else {
                        textEditorModel.deleteBefore();
                    }
                    break;
                case KeyEvent.VK_DELETE:
                    if (textEditorModel.getSelectionRange() != null) {
                        deleteSelectionFunc();
                    } else {
                        textEditorModel.deleteAfter();
                    }
                    break;
                case KeyEvent.VK_SHIFT:
                    isShiftPressed = true;
                    break;
                case KeyEvent.VK_ENTER:
                    textEditorModel.pressEnter();
                    refreshVariables();
                case KeyEvent.VK_CONTROL:
                    isControlPressed = true;
                    break;
                case KeyEvent.VK_C:
                    if (isControlPressed)
                        copyFunc();
                    else {
                        char c = e.getKeyChar();
                        textEditorModel.insert(c);
                    }
                    break;
                case KeyEvent.VK_X:
                    if (isControlPressed) {
                        cutFunc();
                    } else {
                        char c = e.getKeyChar();
                        textEditorModel.insert(c);
                    }
                    break;
                case KeyEvent.VK_V:
                    if (isControlPressed && isShiftPressed) {
                        pasteAndTakeFunc();
                    } else if (isControlPressed) {
                        pasteFunc();
                    } else {
                        char c = e.getKeyChar();
                        textEditorModel.insert(c);
                    }
                    break;
                case KeyEvent.VK_Z:
                    if (isControlPressed) {
                        undoFunc();
                    } else {
                        char c = e.getKeyChar();
                        textEditorModel.insert(c);
                    }
                    break;
                case KeyEvent.VK_Y:
                    if (isControlPressed) {
                        redoFunc();
                    } else {
                        char c = e.getKeyChar();
                        textEditorModel.insert(c);
                    }
                    break;
                default:
                    char c = e.getKeyChar();
                    textEditorModel.insert(c);
            }
        }
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SHIFT:
                    isShiftPressed = false;
                case KeyEvent.VK_CONTROL:
                    isControlPressed = false;
            }
        }
    }

    private void copyFunc() {
        clipboardStack.push(textEditorModel.getSelectedText(textEditorModel.getSelectionRange()));
    }

    private void cutFunc() {
        clipboardStack.push(textEditorModel.getSelectedTextAndDelete(textEditorModel.getSelectionRange()));
        refreshVariables();
    }

    private void pasteFunc() {
        textEditorModel.insert(clipboardStack.peek());
    }

    private void pasteAndTakeFunc() {
        textEditorModel.insert(clipboardStack.pop());
    }

    private void deleteSelectionFunc() {
        textEditorModel.deleteRange(textEditorModel.getSelectionRange());
        refreshVariables();
    }

    private void undoFunc() {
        UndoManager.getInstance().undo();
    }

    private void redoFunc() {
        UndoManager.getInstance().redo();
    }

    private void refreshVariables() {
        textEditorModel.setSelectionRange(null);
        startLocationOfSelection = null;
        shiftMoved = 0;
    }

    private void clearDocument() {
        textEditorModel.clearDocument();
        refreshVariables();
    }

    private void openFile() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int returnedFile = fileChooser.showOpenDialog(null);
        if (returnedFile == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textEditorModel.replaceText(content.toString());
        }
    }

    private void saveFile() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int returnedFile = fileChooser.showSaveDialog(null);
        if (returnedFile == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(textEditorModel.getText());
            bufferedWriter.flush();
        }
    }

    private void loadPlugins() throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String pluginsPath = "C:\\Users\\dujed\\IdeaProjects\\oop_lab3_zad2\\src\\Plugins\\";
        File pluginsDir = new File(pluginsPath);
        File[] jars = pluginsDir.listFiles();
        List<Class<Plugin>> pluginList = new ArrayList<>();
        ClassLoader parent = Plugin.class.getClassLoader();

        assert jars != null;
        for (File jar : jars) {
            URLClassLoader newClassLoader = new URLClassLoader(
                    new URL[]{
                            // Dodaj jedan direktorij (završava s /)
                            new File(pluginsPath).toURI().toURL(),
                            // Dodaj jedan konkretan JAR (ne završava s /)
                            new File(jar.getAbsolutePath()).toURI().toURL()
                    }, parent);
            pluginList.add((Class<Plugin>) newClassLoader.loadClass(jar.getName().substring(0, jar.getName().length() - 4)));
        }
        for (Class<Plugin> plugin : pluginList) {
            Plugin pluginClass = plugin.newInstance();
            JMenuItem menuItem = new JMenuItem(pluginClass.getName());
            plugins.add(menuItem);
            menuItem.addActionListener(a -> pluginClass.execute(textEditorModel, UndoManager.getInstance(), clipboardStack));
        }
    }

    private String putLabelText() {
        return "Cursor location:(" + textEditorModel.getCursorLocation().getX() + "," + textEditorModel.getCursorLocation().getY() + ") ; Number of lines: " + textEditorModel.getLines().size();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextEditor("Oblikovni obrasci u programiranju\nText se nalazi ovdje\nHOHOHOHOHOHOHO\n").setVisible(true));
    }

    class TextPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            FontMetrics fontMetrics = g.getFontMetrics();
            Iterator<String> it = textEditorModel.allLines();
            int i = 0;
            while (it.hasNext()) {
                String line = it.next();
                g.drawString(line, margins, margins + i * lineIndent + fontMetrics.getHeight());
                i++;
            }

            Location location = textEditorModel.getCursorLocation();
            int x1 = fontMetrics.stringWidth(textEditorModel.getLines().get(location.getY()).substring(0, location.getX()));
            g.drawLine(x1 + margins, location.getY() * lineIndent, x1 + margins, lineIndent * location.getY() + fontMetrics.getHeight());

            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            graphics2D.setColor(Color.BLUE);
            LocationRange locationRange = textEditorModel.getSelectionRange();
            if (locationRange != null) {
                if (locationRange.getStart().getY() == locationRange.getEnd().getY()) {
                    int startX = fontMetrics.stringWidth(textEditorModel.getLines().get(location.getY()).substring(0, locationRange.getStart().getX()));
                    int width = fontMetrics.stringWidth(textEditorModel.getLines().get(location.getY()).substring(locationRange.getStart().getX(), locationRange.getEnd().getX()));
                    graphics2D.fillRect(startX + margins, locationRange.getStart().getY() * lineIndent, width + margins, fontMetrics.getHeight());
                } else {
                    int startX = fontMetrics.stringWidth(textEditorModel.getLines().get(locationRange.getStart().getY()).substring(0, locationRange.getStart().getX()));
                    int width = fontMetrics.stringWidth(textEditorModel.getLines().get(locationRange.getStart().getY()).substring(locationRange.getStart().getX()));
                    graphics2D.fillRect(startX + margins, locationRange.getStart().getY() * lineIndent, width + margins, fontMetrics.getHeight());

                    startX = 0;
                    width = fontMetrics.stringWidth(textEditorModel.getLines().get(locationRange.getEnd().getY()).substring(0, locationRange.getEnd().getX()));
                    graphics2D.fillRect(startX + margins, locationRange.getEnd().getY() * lineIndent, width + margins, fontMetrics.getHeight());

                    for (int j = locationRange.getStart().getY() + 1; j < locationRange.getEnd().getY(); j++) {
                        width = fontMetrics.stringWidth(textEditorModel.getLines().get(j));
                        graphics2D.fillRect(0, j * lineIndent, width + margins, fontMetrics.getHeight());
                    }
                }
            }
        }
    }
}
