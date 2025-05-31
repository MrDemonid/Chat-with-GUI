package mr.demonid.view.controllers;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Панель для вывода ANSI-текста.
 */
public class ANSITextPane extends JTextPane {

    private static final char ESC = 27;
    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[([\\d;]+)m");
//    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[0-9;]*m");

    private final Map<Integer, Color> color4bit = new HashMap<>();

    // Текущий атрибут стиля и цвета.
    SimpleAttributeSet currentAttr;


    public ANSITextPane() {
        setEditable(false);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        init4BitColor();
        currentAttr = defaultStyleAttribute();
    }

    public void appendANSI(String input) {
        StyledDocument doc = getStyledDocument();
        Matcher matcher = ANSI_PATTERN.matcher(input);
        int lastIndex = 0;

        while (matcher.find()) {
            String textChunk = input.substring(lastIndex, matcher.start());
            if (!textChunk.isEmpty()) {
                try {
                    doc.insertString(doc.getLength(), textChunk, currentAttr);
                } catch (BadLocationException e) {
                    System.out.println("Bad location: " + e.getMessage());
                }
            }

            // Обработка кодов
            String[] codes = matcher.group(1).split(";");
            int i = 0;
            while (i < codes.length) {
                int code = parseOrZero(codes[i]);

                switch (code) {
                    case 0:  // Reset all
                        currentAttr = defaultStyleAttribute();
                        break;
                    case 1: StyleConstants.setBold(currentAttr, true); break;
                    case 3: StyleConstants.setItalic(currentAttr, true); break;
                    case 4: StyleConstants.setUnderline(currentAttr, true); break;
                    case 9: StyleConstants.setStrikeThrough(currentAttr, true); break;
                    case 22: StyleConstants.setBold(currentAttr, false); break;
                    case 23: StyleConstants.setItalic(currentAttr, false); break;
                    case 24: StyleConstants.setUnderline(currentAttr, false); break;
                    case 29: StyleConstants.setStrikeThrough(currentAttr, false); break;

                    // 38 — extended foreground
                    case 38:
                        if (i + 1 < codes.length) {
                            int mode = parseOrZero(codes[++i]);
                            if (mode == 5 && i + 1 < codes.length) {
                                // 8-битный цвет
                                int colorIndex = parseOrZero(codes[++i]);
                                StyleConstants.setForeground(currentAttr, get256Color(colorIndex));
                            } else if (mode == 2 && i + 3 < codes.length) {
                                // RGB
                                int r = parseOrZero(codes[++i]);
                                int g = parseOrZero(codes[++i]);
                                int b = parseOrZero(codes[++i]);
                                StyleConstants.setForeground(currentAttr, new Color(r, g, b));
                            }
                        }
                        break;

                    // 48 — extended background
                    case 48:
                        if (i + 1 < codes.length) {
                            int mode = parseOrZero(codes[++i]);
                            if (mode == 5 && i + 1 < codes.length) {
                                int colorIndex = parseOrZero(codes[++i]);
                                StyleConstants.setBackground(currentAttr, get256Color(colorIndex));
                            } else if (mode == 2 && i + 3 < codes.length) {
                                int r = parseOrZero(codes[++i]);
                                int g = parseOrZero(codes[++i]);
                                int b = parseOrZero(codes[++i]);
                                StyleConstants.setForeground(currentAttr, new Color(r, g, b));
                            }
                        }
                        break;
                    default:
                        // 30–37: foreground, 90–97: bright foreground
                        // 40–47: background, 100–107: bright background
                        if ((code >= 30 && code <= 37) || (code >= 90 && code <= 97)) {
                            StyleConstants.setForeground(currentAttr, color4bit.get(code));
                        } else if ((code >= 40 && code <= 47) || (code >= 100 && code <= 107)) {
                            StyleConstants.setBackground(currentAttr, color4bit.get(code-10));
                        }
                }

                i++;
            }

            lastIndex = matcher.end();
        }

        // Остаток строки
        if (lastIndex < input.length()) {
            try {
                doc.insertString(doc.getLength(), input.substring(lastIndex), currentAttr);
            } catch (BadLocationException e) {
                System.out.println("Bad location end: " + e.getMessage());
            }
        }
    }

    /**
     * Перевод строки в число.
     * @param s Строка.
     * @return Число, а если была ошибка, то вернет ноль.
     */
    private int parseOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Получение 8-битного цвета.
     */
    private Color get256Color(int index) {
        if (index >= 0 && index <= 15) {
            return color4bit.get(index < 8 ? index + 30 : index + 82);
        } else if (index >= 16 && index <= 231) {
            int base = index - 16;
            int r = base / 36;
            int g = (base / 6) % 6;
            int b = base % 6;
            return new Color(r * 51, g * 51, b * 51);
        } else if (index >= 232 && index <= 255) {
            int gray = (index - 232) * 10 + 8;
            return new Color(gray, gray, gray);
        } else {
            return Color.WHITE;
        }
    }

    /**
     * Инициализация карты стандартных цветов ANSI
     */
    private void init4BitColor() {
        color4bit.put(30,Color.BLACK);
        color4bit.put(31,Color.RED);
        color4bit.put(32,Color.GREEN);
        color4bit.put(33,Color.YELLOW);
        color4bit.put(34,Color.BLUE);
        color4bit.put(35,Color.MAGENTA);
        color4bit.put(36,Color.CYAN);
        color4bit.put(37,Color.LIGHT_GRAY);
        color4bit.put(90, new Color(128, 128, 128));
        color4bit.put(91, Color.RED.brighter());
        color4bit.put(92, Color.GREEN.brighter());
        color4bit.put(93, Color.YELLOW.brighter());
        color4bit.put(94, Color.BLUE.brighter());
        color4bit.put(95, Color.MAGENTA.brighter());
        color4bit.put(96, Color.CYAN.brighter());
        color4bit.put(97, Color.WHITE);
    }

    private SimpleAttributeSet defaultStyleAttribute() {
        SimpleAttributeSet res = new SimpleAttributeSet();
        StyleConstants.setForeground(res, Color.BLACK);
        StyleConstants.setBackground(res, Color.WHITE);
        StyleConstants.setBold(res, false);
        StyleConstants.setItalic(res, false);
        StyleConstants.setUnderline(res, false);
        StyleConstants.setStrikeThrough(res, false);
        return res;
    }

}
