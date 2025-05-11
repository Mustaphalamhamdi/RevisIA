package com.mustapha.revisia.util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class UIUtils {

    /**
     * Creates an AI badge that can be added to UI components
     * @param tooltipText Optional custom tooltip text (or null for default)
     * @return A Label styled as an AI badge
     */
    public static Node createAIBadge(String tooltipText) {
        Label aiBadge = new Label("IA");
        aiBadge.getStyleClass().add("ai-badge");

        // Add a tooltip explaining the AI feature
        String text = tooltipText != null ? tooltipText :
                "Cette fonctionnalité est alimentée par l'intelligence artificielle";
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(aiBadge, tooltip);

        return aiBadge;
    }

    /**
     * Creates an AI badge with default tooltip
     * @return A Label styled as an AI badge
     */
    public static Node createAIBadge() {
        return createAIBadge(null);
    }
}