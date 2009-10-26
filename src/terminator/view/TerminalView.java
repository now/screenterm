package terminator.view;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import e.gui.*;
import e.util.*;

import terminator.*;
import terminator.model.*;
import terminator.terminal.*;

public class TerminalView extends JComponent implements FocusListener, TerminalListener {
	private static final Stopwatch paintComponentStopwatch = Stopwatch.get("TerminalView.paintComponent");
	private static final Stopwatch paintStyledTextStopwatch = Stopwatch.get("TerminalView.paintStyledText");
        private static final Font font = new Font("DejaVu Sans Mono", Font.PLAIN, 14);
	
	private TerminalModel model;
        private CursorPainter cursorPainter;
	
	public TerminalView(TerminalModel model) {
		ComponentUtilities.disableFocusTraversal(this);
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setOpaque(true);
                setForeground(Style.DEFAULT.foreground());
                setBackground(Style.DEFAULT.background());
                setFont(font);

                this.model = model;
                cursorPainter = new UnfocusedCursorPainter();
                model.addListener(this);

                setFixedSize(optimalViewSize());

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
                Insets insets = getInsets();
                return new Dimension(insets.left + size.width * character.width + insets.right,
                                     insets.top + size.height * character.height + insets.bottom);
        }

        public Dimension sizeInCharacters(Dimension size) {
                Dimension result = new Dimension(size);
                Insets insets = getInsets();
                result.width -= (insets.left + insets.right);
                result.height -= (insets.top + insets.bottom);
                Dimension character = characterSize();
                result.width /= character.width;
                result.height /= character.height;
                return result;
        }

	public void userIsTyping() {
                redrawCursorPosition();
                setCursor(GuiUtilities.INVISIBLE_CURSOR);
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
		revalidate();
                repaint();
	}

	public void contentsChanged(int row) {
		Point redrawTop = modelToView(row, 0).getLocation();
		Dimension size = getSize();
		repaint(redrawTop.x, redrawTop.y, size.width, size.height - redrawTop.y);
	}
	
	public void cursorPositionChanged(terminator.model.Cursor oldCursorPosition, terminator.model.Cursor newCursorPosition) {
                redrawPosition(oldCursorPosition);
		redrawCursorPosition();
	}

        public void cursorVisibilityChanged(boolean isVisible) {
                redrawCursorPosition();
        }

        private Rectangle modelToView(terminator.model.Cursor cursor) {
                return modelToView(cursor.row(), cursor.column());
        }

	private Rectangle modelToView(int row, int column) {
                String line = model.getLine(row);
                String c = column < line.length() ? line.substring(column, column + 1) : " ";
                String prefix = column < line.length() ? line.substring(0, column) : line;
                FontMetrics metrics = getFontMetrics(getFont());
                Insets insets = getInsets();
                int x = insets.left +
                        metrics.stringWidth(prefix) +
                        (column < line.length() ?
                                0 :
                                metrics.stringWidth(" ") * (column - line.length()));
                int width = metrics.stringWidth(c);
                int height = metrics.getHeight();
                int y = insets.top + row * height;
                return new Rectangle(x, y, width, height);
	}
	
	private void redrawCursorPosition() {
                redrawPosition(model.getCursor());
	}

        private void redrawPosition(terminator.model.Cursor p) {
                repaint(modelToView(p));
        }
	
	public void paintComponent(Graphics oldGraphics) {
		Stopwatch.Timer timer = paintComponentStopwatch.start();
		try {
                        Graphics2D g = (Graphics2D)oldGraphics;
			
                        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			Rectangle rect = g.getClipBounds();
			g.setColor(getBackground());
			g.fill(rect);
			
                        FontMetrics metrics = getFontMetrics(getFont());
                        int charHeight = Math.max(metrics.getHeight(), 1);
                        Insets insets = getInsets();
                        int first = (rect.y - insets.top) / charHeight;
                        int last = (rect.y - insets.top + rect.height + charHeight - 1) / charHeight;
                        int baseline = insets.top + charHeight * first - metrics.getMaxDescent();
                        int maxX = rect.x + rect.width;
                        for (TextLine line : model.region(first, last)) {
                                baseline += charHeight;
                                paintLine(g, metrics, line, insets.left, baseline, maxX);
                        }
                        cursorPainter.paint(g, first, last);
		} finally {
			timer.stop();
		}
	}

        private void paintLine(Graphics2D g, FontMetrics metrics,
                               TextLine line, int x, int y, int maxX) {
                for (StyledText text : line.styledTexts()) {
                        if (x >= maxX)
                                break;
                        x += paintStyledText(g, metrics, text, x, y);
                }
        }

        private int paintStyledText(Graphics2D g, FontMetrics metrics,
                                    StyledText text, int x, int y) {
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
                        
                        int width = metrics.stringWidth(text.getText());
                        if (!background.equals(getBackground()))
                                paintBackground(g, metrics, x, y, width, background);
                        if (style.underline())
                                paintUnderline(g, metrics, x, y, width, foreground);
                        g.setColor(foreground);
                        g.drawString(text.getText(), x, y);
                        return width;
                } finally {
                        timer.stop();
                }
        }

        private void paintBackground(Graphics2D g, FontMetrics m, int x, int y,
                                     int width, Color c) {
                g.setColor(c);
                g.fillRect(x, y - m.getMaxAscent() - m.getLeading(),
                           width, m.getHeight());
        }

        private void paintUnderline(Graphics2D g, FontMetrics m, int x, int y,
                                    int width, Color c) {
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
                g.drawLine(x, y + 1, x + width, y + 1);
        }

        private abstract class CursorPainter {
                public void paint(Graphics2D g, int firstLine, int lastLine) {
                        terminator.model.Cursor p = model.getCursor();

                        if (!p.isInsideLines(firstLine, lastLine))
                                return;
                        g.setColor(Color.black);
                        paintCursor(g, modelToView(p));
                }

                protected abstract void paintCursor(Graphics2D g, Rectangle r);
        }

        private class FocusedCursorPainter extends CursorPainter {
                @Override
                protected void paintCursor(Graphics2D g, Rectangle r) {
                        g.setXORMode(Color.white);
                        g.fill(r);
                        g.setPaintMode();
                }
        }


        private class UnfocusedCursorPainter extends CursorPainter {
                @Override
                protected void paintCursor(Graphics2D g, Rectangle r) {
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
