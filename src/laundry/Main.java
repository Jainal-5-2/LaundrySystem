package laundry;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.Color;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        // Global FlatLaf tweaks — shape & motion
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 999);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

        // Global colors — map FlatLaf defaults onto our Theme
        UIManager.put("Component.focusColor", Theme.ACCENT);
        UIManager.put("Component.borderColor", Theme.BORDER_SUBTLE);
        UIManager.put("Component.disabledBorderColor", Theme.BORDER_SUBTLE);
        UIManager.put("Component.focusedBorderColor", Theme.ACCENT);

        UIManager.put("Panel.background", Theme.BG_BOTTOM);
        UIManager.put("OptionPane.background", Theme.BG_CARD);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT_PRIMARY);

        UIManager.put("Label.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("Label.disabledForeground", Theme.TEXT_MUTED);

        UIManager.put("Button.background", Theme.ACCENT);
        UIManager.put("Button.foreground", Theme.TEXT_ON_ACCENT);
        UIManager.put("Button.hoverBackground", Theme.ACCENT_HOVER);
        UIManager.put("Button.pressedBackground", Theme.ACCENT_DARK);
        UIManager.put("Button.disabledBackground", Theme.BORDER_SUBTLE);
        UIManager.put("Button.default.boldText", true);

        UIManager.put("TextField.background", Theme.BG_INPUT);
        UIManager.put("TextField.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("TextField.placeholderForeground", Theme.TEXT_MUTED);
        UIManager.put("TextField.selectionBackground", Theme.ACCENT);
        UIManager.put("TextField.selectionForeground", Theme.TEXT_ON_ACCENT);
        UIManager.put("TextField.caretForeground", Theme.ACCENT);

        UIManager.put("PasswordField.background", Theme.BG_INPUT);
        UIManager.put("PasswordField.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", Theme.ACCENT);
        UIManager.put("PasswordField.selectionBackground", Theme.ACCENT);
        UIManager.put("PasswordField.selectionForeground", Theme.TEXT_ON_ACCENT);

        UIManager.put("ComboBox.background", Theme.BG_INPUT);
        UIManager.put("ComboBox.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", Theme.ACCENT_SOFT);
        UIManager.put("ComboBox.selectionForeground", Theme.TEXT_PRIMARY);
        UIManager.put("ComboBox.buttonBackground", Theme.BG_INPUT);
        UIManager.put("ComboBox.buttonArrowColor", Theme.TEXT_SECONDARY);

        UIManager.put("Table.background", Theme.BG_CARD);
        UIManager.put("Table.alternateRowColor", new Color(0xF9FAFB));
        UIManager.put("Table.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("Table.gridColor", Theme.BORDER_SUBTLE);
        UIManager.put("Table.selectionBackground", Theme.ACCENT_SOFT);
        UIManager.put("Table.selectionForeground", Theme.TEXT_PRIMARY);
        UIManager.put("TableHeader.background", Theme.BG_BOTTOM);
        UIManager.put("TableHeader.foreground", Theme.TEXT_SECONDARY);
        UIManager.put("TableHeader.separatorColor", Theme.BORDER_SUBTLE);
        UIManager.put("TableHeader.bottomSeparatorColor", Theme.BORDER_SUBTLE);

        UIManager.put("ScrollPane.background", Theme.BG_BOTTOM);
        UIManager.put("ScrollPane.borderColor", Theme.BORDER_SUBTLE);
        UIManager.put("ScrollBar.thumb", new Color(0xD1D5DB));
        UIManager.put("ScrollBar.hoverThumbColor", new Color(0x9CA3AF));
        UIManager.put("ScrollBar.track", Theme.BG_BOTTOM);

        UIManager.put("Separator.foreground", Theme.BORDER_SUBTLE);
        UIManager.put("ToolTip.background", Theme.TEXT_PRIMARY);
        UIManager.put("ToolTip.foreground", Color.WHITE);

        // Tabs
        UIManager.put("TabbedPane.tabHeight", 38);
        UIManager.put("TabbedPane.tabArc", 14);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.selectedBackground", Theme.BG_CARD);
        UIManager.put("TabbedPane.underlineColor", Theme.ACCENT);
        UIManager.put("TabbedPane.focusColor", Theme.ACCENT);
        UIManager.put("TabbedPane.hoverColor", Theme.ACCENT_SOFT);
        UIManager.put("TabbedPane.contentAreaColor", Theme.BG_CARD);

        // SwingX date picker
        UIManager.put("JXDatePicker.arrowDown.color", Theme.TEXT_SECONDARY);
        UIManager.put("JXMonthView.background", Theme.BG_CARD);
        UIManager.put("JXMonthView.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("JXMonthView.monthStringBackground", Theme.BG_CARD);
        UIManager.put("JXMonthView.monthStringForeground", Theme.TEXT_PRIMARY);
        UIManager.put("JXMonthView.daysOfTheWeekForeground", Theme.TEXT_SECONDARY);
        UIManager.put("JXMonthView.weekOfTheYearForeground", Theme.TEXT_MUTED);
        UIManager.put("JXMonthView.unselectableDayForeground", Theme.TEXT_MUTED);
        UIManager.put("JXMonthView.selectedBackground", Theme.ACCENT);
        UIManager.put("JXMonthView.flaggedDayForeground", Theme.ERROR);
        UIManager.put("JXMonthView.trailingDaysForeground", Theme.TEXT_MUTED);

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}