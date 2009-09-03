package terminator.view;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import terminator.*;
import terminator.view.highlight.*;

public class FindPanel extends JPanel implements FindStatusDisplay {
    private final JTerminalPane terminalPane;
    
    private final JTextField findField = new JTextField(20);
    private final ELabel findStatus = new ELabel();
    
    private final Timer typingTimer;
    
    private final TerminatorMenuBar.BindableAction findNextAction = new TerminatorMenuBar.FindNextAction();
    private final TerminatorMenuBar.BindableAction findPreviousAction = new TerminatorMenuBar.FindPreviousAction();
    
    public FindPanel(JTerminalPane terminalPane) {
        this.terminalPane = terminalPane;
        
        this.typingTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateResults();
            }
        });
        typingTimer.setRepeats(false);
        findField.getDocument().addDocumentListener(new DocumentAdapter() {
            public void documentChanged() {
                typingTimer.restart();
            }
        });
        findField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                hideFindPanel();
            }
        });
        
        initKeyBindings();
        
        JComponent findComponent;
        if (GuiUtilities.isMacOs() && System.getProperty("os.version").startsWith("10.4") == false) {
            findField.putClientProperty("JTextField.variant", "search");
            findComponent = findField;
        } else {
            findComponent = labeledComponent("Find:", findField);
        }
        
        setLayout(new BorderLayout(8, 0));
        add(Box.createVerticalStrut(2), BorderLayout.NORTH);
        add(findComponent, BorderLayout.WEST);
        add(findStatus, BorderLayout.CENTER);
        
        findStatus.setFont(UIManager.getFont("ToolTip.font"));
    }
    
    private JComponent labeledComponent(String label, JComponent component) {
        final JPanel result = new JPanel(new BorderLayout(4, 0));
        result.add(new ELabel(label), BorderLayout.WEST);
        result.add(component, BorderLayout.CENTER);
        return result;
    }
    
    private void initKeyBindings() {
        findNextAction.bindTo(terminalPane);
        ComponentUtilities.initKeyBinding(findField, findNextAction);
        
        findPreviousAction.bindTo(terminalPane);
        ComponentUtilities.initKeyBinding(findField, findPreviousAction);
        
        ComponentUtilities.initKeyBinding(findField, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), new EnterAction());
        ComponentUtilities.initKeyBinding(findField, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new EscapeAction());
        
        // FIXME: add up- and down-arrow bindings to pull old searches from a StringHistory.
    }
    
    private class EnterAction extends AbstractAction {
        public EnterAction() {
            super("EnterAction");
        }
        
        public void actionPerformed(ActionEvent e) {
            // Enter hides the find panel but leaves the search active.
            hideFindPanel();
        }
    }
    
    private class EscapeAction extends AbstractAction {
        public EscapeAction() {
            super("EscapeAction");
        }
        
        public void actionPerformed(ActionEvent e) {
            // Escape hides the find panel and cancels the search.
            getFindHighlighter().forgetPattern(terminalPane.getTerminalView());
            hideFindPanel();
        }
    }
    
    private void hideFindPanel() {
        terminalPane.requestFocus();
        setVisible(false);
    }
    
    private void updateResults() {
        getFindHighlighter().setPattern(terminalPane.getTerminalView(), findField.getText(), this);
    }
    
    private FindHighlighter getFindHighlighter() {
        return terminalPane.getTerminalView().getHighlighterOfClass(FindHighlighter.class);
    }
    
    public void showFindPanel() {
        final String selection = terminalPane.getSelectionHighlighter().getTabbedString();
        if (selection.length() > 0) {
            findField.setText("(?-i)" + StringUtilities.regularExpressionFromLiteral(selection));
        }
        updateResults();
        findField.selectAll();
        setVisible(true);
        findField.requestFocus();
    }
    
    public void setStatus(String text, boolean isError) {
        findField.setForeground(isError ? Color.RED : UIManager.getColor("TextField.foreground"));
        findStatus.setText(text);
    }
}
