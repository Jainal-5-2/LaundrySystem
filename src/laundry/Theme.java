package laundry;

import java.awt.Color;

public final class Theme {
    private Theme() {}

    // ----- Accent (indigo) -----
    public static final Color ACCENT        = new Color(0x6366F1); // indigo-500
    public static final Color ACCENT_HOVER  = new Color(0x4F46E5); // indigo-600
    public static final Color ACCENT_DARK   = new Color(0x4338CA); // indigo-700
    public static final Color ACCENT_SOFT   = new Color(0xEEF2FF); // indigo-50
    public static final Color ACCENT_SOFT_HOVER = new Color(0xE0E7FF); // indigo-100

    // ----- Surfaces (light theme) -----
    public static final Color BG_TOP        = new Color(0xFFFFFF);
    public static final Color BG_BOTTOM     = new Color(0xFAFAFB);
    public static final Color BG_CARD       = new Color(0xFFFFFF);
    public static final Color BG_INPUT      = new Color(0xF3F4F6); // gray-100
    public static final Color BORDER_SUBTLE = new Color(0xE5E7EB); // gray-200
    public static final Color BORDER_STRONG = new Color(0xD1D5DB); // gray-300

    // ----- Text -----
    public static final Color TEXT_PRIMARY   = new Color(0x111827); // gray-900
    public static final Color TEXT_SECONDARY = new Color(0x6B7280); // gray-500
    public static final Color TEXT_MUTED     = new Color(0x9CA3AF); // gray-400
    public static final Color TEXT_ON_ACCENT = Color.WHITE;

    // ----- Status -----
    public static final Color ERROR         = new Color(0xEF4444); // red-500
    public static final Color ERROR_SOFT    = new Color(0xFEE2E2);
    public static final Color SUCCESS       = new Color(0x10B981); // emerald-500
    public static final Color SUCCESS_SOFT  = new Color(0xD1FAE5);
    public static final Color WARNING       = new Color(0xF59E0B); // amber-500

    // ----- Brand gradient (used on the login branding panel) -----
    public static final Color BRAND_TOP     = new Color(0x6366F1); // indigo-500
    public static final Color BRAND_BOTTOM  = new Color(0x8B5CF6); // violet-500

    // ----- Shadow -----
    public static final Color SHADOW        = new Color(0, 0, 0, 12);
}