package gui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import tracking.shared.Indicator;
import tracking.shared.Unit;
import ui.shared.IUI;

public class AsciiWatchGui implements WatchGui {

    private static final int[][] DIGIT_COORDS = {
        new int[] {14, 6},
        new int[] {17, 6},
        new int[] {21, 6},
        new int[] {24, 6}
    };

    private static final int DATA_X = 14;
    private static final int DATA_Y = 5;
    private static final int DATA_LEN = 13;

    private static final String[] UNIT_LABELS = new String[] {
        "km", 
        "meters",
        "min/km",
        "km/h",
        "miles",
        "yds",
        "ft",
        "min/mile",
        "mph",
        "bpm",
        "laps"
    };
    private static final int UNIT_KM           = 0;
    private static final int UNIT_METERS       = 1;
    private static final int UNIT_MIN_PER_KM   = 2;
    private static final int UNIT_KM_PER_HOUR  = 3;
    private static final int UNIT_MILES        = 4;
    private static final int UNIT_YDS          = 5;
    private static final int UNIT_FT           = 6;
    private static final int UNIT_MIN_PER_MILE = 7;
    private static final int UNIT_MPH          = 8;
    private static final int UNIT_BPM          = 9;
    private static final int UNIT_LAPS         = 10;

    private TextImage[] numerals;
    private TextImage watchFace;

    private Terminal terminal;
    private TextGraphics watchGraphics;

    private Gui signalHandler;

    public AsciiWatchGui(Gui gui) {
        // set to headless mode
        System.setProperty("java.awt.headless", "true");

        // set the gui connection
        signalHandler = gui;

        // load resources
        numerals = loadNumerals();
        watchFace = loadWatchFace();

        try {
            // create terminal
            terminal = new DefaultTerminalFactory().createTerminal();
            terminal.setCursorVisible(false);
            terminal.clearScreen();

            // draw watch graphics
            watchGraphics = terminal.newTextGraphics();
            watchGraphics.drawImage(TerminalPosition.TOP_LEFT_CORNER, watchFace);

            // set standard error to print out beneath the watch
            System.setErr(new PrintStream(new TerminalOutputStream(terminal, 0, watchFace.getSize().getRows())));
        } catch (IOException e) { /* do nothing */ }
    }

    @Override
    public void display() {
        setTime(0);
        setData(0, Unit.METERS);
        char command = ' ';
        while(command != 'x') {
            try {
                command = terminal.readInput().getCharacter();
                switch (command) {
                case 's':
                    signalHandler.sendSignal(new IUI.StartStopPressed());
                    System.err.println("Sending start/stop");
                    break;
                case 'r':
                    signalHandler.sendSignal(new IUI.LapResetPressed());
                    System.err.println("Sending reset/lap");
                    break;
                case 'm':
                    signalHandler.sendSignal(new IUI.ModePressed());
                    System.err.println("Sending mode");
                    break;
                }
            } catch (IOException e) {
                command = 'x';
            }
        }
        System.err.println("Exiting...");
            
        // clear screen and exit
        try {
            terminal.setCursorPosition(0, 0);
            terminal.clearScreen();
        } catch (IOException e) { /* do nothing */ }
        System.exit(0);
    }

    @Override
    public void setData(float value, Unit unit) {
        String valueString = "";
        String unitString = UNIT_LABELS[unit.getValue()];
        switch (unit.getValue()) {
        case UNIT_KM:
        case UNIT_MILES:        
        case UNIT_KM_PER_HOUR:
        case UNIT_MPH:
            valueString = String.format("%.2f", value);
            break;
        case UNIT_METERS:
        case UNIT_YDS:
        case UNIT_FT:
        case UNIT_BPM:
        case UNIT_LAPS:
            valueString = String.format("%d", (int)value);
            break;
        case UNIT_MIN_PER_KM:
        case UNIT_MIN_PER_MILE:
            valueString = String.format("%d:%02d", (int)value % 60, (int)(60 * value) % 60);
            break;
        default:
            break;
        }
        int padding = DATA_LEN - (valueString.length() + unitString.length());
        if (padding < 1) {
            padding = 1;
        }
        String data = valueString;
        for (int i = 0; i < padding; i++) {
            data += " ";
        }
        data += unitString;
        if (data.length() > DATA_LEN) {
            data = data.substring(0, DATA_LEN);
        }
        while (data.length() < DATA_LEN) {
            data += " ";
        }
        watchGraphics.putString(new TerminalPosition(DATA_X, DATA_Y), data);
    }

    @Override
    public void setTime(int time) {
        int min = (time % 3600) / 60;
        int sec = time % 60;
        watchGraphics.drawImage(new TerminalPosition(DIGIT_COORDS[0][0], DIGIT_COORDS[0][1]), numerals[(min / 10) % 10]);
        watchGraphics.drawImage(new TerminalPosition(DIGIT_COORDS[1][0], DIGIT_COORDS[1][1]), numerals[min % 10]);
        watchGraphics.drawImage(new TerminalPosition(DIGIT_COORDS[2][0], DIGIT_COORDS[2][1]), numerals[(sec / 10) % 10]);
        watchGraphics.drawImage(new TerminalPosition(DIGIT_COORDS[3][0], DIGIT_COORDS[3][1]), numerals[sec % 10]);
    }

    @Override
    public void setIndicator(Indicator value) {
        // not implemented
    }

    private TextImage[] loadNumerals() {
        List<TextImage> numerals = new ArrayList<>();
        List<String> lines = new ArrayList<>();;
        URL txtURL = ClassLoader.getSystemResource("gui/txt/numbers.txt");
        try {
            Scanner sc = new Scanner(txtURL.openStream());
            while(sc.hasNextLine()) {
                if (lines.size() >= 3) {
                    numerals.add(fromLines(lines));
                    lines = new ArrayList<>();
                }
                lines.add(sc.nextLine());
            }
            numerals.add(fromLines(lines));
            sc.close();
        } catch (IOException e) { /* do nothing */ }
        return numerals.toArray(new TextImage[0]);
    }

    private TextImage loadWatchFace() {
        List<String> lines = new ArrayList<>();
        URL txtURL = ClassLoader.getSystemResource("gui/txt/watchface.txt");
        try {
            Scanner sc = new Scanner(txtURL.openStream());
            while(sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
            sc.close();
        } catch (IOException e) { /* do nothing */ }
        return fromLines(lines);
    }

    private TextImage fromLines(List<String> lines) {
        int numColumns = lines.stream().max((a, b) -> a.length() - b.length()).get().length();
        int numLines = lines.size();
        TextImage newImage = new BasicTextImage(numColumns, numLines);
        newImage.setAll(new TextCharacter(' '));
        for (int y = 0; y < lines.size(); y++) {
            for (int x = 0; x < lines.get(y).length(); x++) {
                newImage.setCharacterAt(x, y, new TextCharacter(lines.get(y).charAt(x)));
            }
        }
        return newImage;
    }

    private class TerminalOutputStream extends OutputStream {

        private int cursorX;
        private int cursorY;
        private Terminal terminal;

        public TerminalOutputStream(Terminal t, int x, int y) {
            terminal = t;
            cursorX = x;
            cursorY = y;
        }

        public void write(int b) {
            char c = (char)b;
            try {
                terminal.setCursorPosition(cursorX, cursorY);
                terminal.putCharacter(c);
                if (c == '\n') {
                    cursorY++;
                    cursorX = 0;
                }
                else {
                    cursorX++;
                }
            } catch (IOException e) { /* do nothing */ }
        }

    }

}
