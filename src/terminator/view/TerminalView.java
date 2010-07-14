package terminator.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

import terminator.model.*;
import terminator.model.Cursor;
import terminator.util.*;

public class TerminalView extends JComponent implements FocusListener, TerminalListener {
	private static final Stopwatch paintComponentStopwatch = Stopwatch.get("TerminalView.paintComponent");
	private static final Stopwatch paintStyledTextStopwatch = Stopwatch.get("TerminalView.paintStyledText");
        private static final Font font = new Font("DejaVu Sans Mono", Font.PLAIN, 14);
        private static final java.awt.Cursor invisibleCursor =
          Toolkit.getDefaultToolkit().createCustomCursor(
            new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR),
            new Point(0, 0),
            "invisible");

	private TerminalModel model;
        private CursorPainter cursorPainter;
	
	public TerminalView(TerminalModel model) {
                setFont(font);

                this.model = model;
                cursorPainter = new UnfocusedCursorPainter();
                model.addListener(this);

                setFixedSize(optimalViewSize());

                disableFocusTraversal();

		addFocusListener(this);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				requestFocus();
			}
		});
                addMouseMotionListener(new MouseMotionAdapter() {
                        public void mouseMoved(MouseEvent event) {
                                setCursor(java.awt.Cursor.getDefaultCursor());
                        }
                });
	}

        private Dimension optimalViewSize() {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension adjustedSize = new Dimension((int)(0.85 * screenSize.width), screenSize.height);
                Dimension size = sizeInCharacters(adjustedSize);
                size.width = Math.min(size.width, 132);
                Dimension character = characterSize();
                return applyInsets(new Dimension(size.width * character.width,
                                                 size.height * character.height),
                                   1);
        }

        private Dimension applyInsets(Dimension d, int factor) {
                Insets i = getInsets();
                return new Dimension(d.width + factor * (i.left + i.right),
                                     d.height + factor * (i.top + i.bottom));
        }

        public Dimension sizeInCharacters() {
                return sizeInCharacters(getSize());
        }

        private Dimension sizeInCharacters(Dimension size) {
                Dimension character = characterSize();
                size = applyInsets(size, -1);
                return new Dimension(size.width / character.width,
                                     size.height / character.height);
        }

        private Dimension characterSize() {
                FontMetrics metrics = getFontMetrics(getFont());
                return new Dimension(Math.max(metrics.charWidth('W'), 1),
                                     Math.max(metrics.getHeight(), 1));
        }

        private void setFixedSize(Dimension size) {
                setMaximumSize(size);
                setPreferredSize(size);
                setSize(size);
        }

        private void disableFocusTraversal() {
          Set<AWTKeyStroke> empty = Collections.emptySet();
          setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, empty);
          setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, empty);
        }

	public void userIsTyping() {
                redrawCursorPosition();
                setCursor(invisibleCursor);
	}

	public void contentsChanged(int row) {
		Point top = modelToView(row, 0).getLocation();
		Dimension size = getSize();
		repaint(top.x, top.y, size.width, size.height - top.y);
	}

        public void cursorPositionChanged(Cursor oldCursor, Cursor newCursor) {
                redrawPosition(oldCursor);
		redrawCursorPosition();
        }

        public void cursorVisibilityChanged(boolean isVisible) {
                redrawCursorPosition();
        }

        private Rectangle modelToView(Cursor cursor) {
                return modelToView(cursor.row(), cursor.column());
        }

	private Rectangle modelToView(int row, int column) {
                FontMetrics m = getFontMetrics(getFont());
                Insets i = getInsets();
                String line = model.getLine(row);
                String prefix = line.substring(0, Math.min(column, line.length()));
                int x = i.left + m.stringWidth(prefix);
                String c = " ";
                if (column < line.length())
                        c = line.substring(column, column + 1);
                else
                        x += m.stringWidth(" ") * (column - line.length());
                int y = i.top + row * m.getHeight();
                return new Rectangle(x, y, m.stringWidth(c), m.getHeight());
	}
	
	private void redrawCursorPosition() {
                redrawPosition(model.getCursor());
	}

        private void redrawPosition(Cursor cursor) {
                repaint(modelToView(cursor));
        }
	
        public void paintComponent(Graphics graphics) {
		Stopwatch.Timer timer = paintComponentStopwatch.start();
		try {
                        Graphics2D g = (Graphics2D)graphics;

                        Rectangle r = g.getClipBounds();
                        g.setColor(Style.DEFAULT.background());
                        g.fill(r);

                        FontMetrics m = g.getFontMetrics();
                        int h = Math.max(m.getHeight(), 1);
                        Insets i = getInsets();
                        paintLines(g, (r.y - i.top) / h, (r.height + h - 1) / h, i.left,
                                   r.y + h - m.getMaxDescent(), r.x + r.width);
		} finally {
			timer.stop();
		}
	}

        private void paintLines(Graphics2D g, int first, int count,
                                int x, int y, int maxX) {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                for (TextLine line : model.region(first, first + count))
                        y += paintLine(g, line, x, y, maxX);
                cursorPainter.paint(g, first, first + count);
        }

        private int paintLine(Graphics2D g, TextLine line, int x, int y, int maxX) {
                for (StyledText text : line.styledTexts()) {
                        if (x >= maxX)
                                break;
                        x += paintStyledText(g, text, x, y);
                }
                return g.getFontMetrics().getHeight();
        }

        private int paintStyledText(Graphics2D g, StyledText text, int x, int y) {
                Stopwatch.Timer timer = paintStyledTextStopwatch.start();
                try {
                        Style style = text.getStyle();
                        Color foreground = style.foreground();
                        Color background = style.background();
                        
                        if (style.reverseVideo()) {
                                Color oldForeground = foreground;
                                foreground = background;
                                background = oldForeground;
                        }
                        
                        int width = g.getFontMetrics().stringWidth(text.getText());
                        if (!background.equals(getBackground()))
                                paintBackground(g, x, y, width, background);
                        if (style.underline())
                                paintUnderline(g, x, y, width, foreground);
                        g.setColor(foreground);
                        g.drawString(text.getText(), x, y);
                        return width;
                } finally {
                        timer.stop();
                }
        }

        private void paintBackground(Graphics2D g, int x, int y, int width, Color c) {
                FontMetrics m = g.getFontMetrics();
                g.setColor(c);
                g.fillRect(x, y - m.getMaxAscent() - m.getLeading(),
                           width, m.getHeight());
        }

        private void paintUnderline(Graphics2D g, int x, int y, int width, Color c) {
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
                g.drawLine(x, y + 1, x + width, y + 1);
        }

        private abstract class CursorPainter {
                public void paint(Graphics2D g, int first, int last) {
                        Cursor cursor = model.getCursor();

                        if (!cursor.isInsideLines(first, last))
                                return;
                        g.setColor(Color.black);
                        paintCursor(g, modelToView(cursor));
                }

                protected abstract void paintCursor(Graphics2D g, Rectangle r);
        }

        private class FocusedCursorPainter extends CursorPainter {
                @Override protected void paintCursor(Graphics2D g, Rectangle r) {
                        g.setXORMode(Color.white);
                        g.fill(r);
                        g.setPaintMode();
                }
        }

        private class UnfocusedCursorPainter extends CursorPainter {
                @Override protected void paintCursor(Graphics2D g, Rectangle r) {
                        g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
                }
        }

	public void focusGained(FocusEvent event) {
                setCursorPainter(new FocusedCursorPainter());
	}
	
	public void focusLost(FocusEvent event) {
                setCursorPainter(new UnfocusedCursorPainter());
	}

        private void setCursorPainter(CursorPainter cursorPainter) {
                this.cursorPainter = cursorPainter;
                redrawCursorPosition();
        }
}
