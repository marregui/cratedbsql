/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */
package io.crate.cli.common;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;


public final class GUIToolkit {

    public static final String JDBC_DRIVER_URL_FORMAT = "jdbc:postgresql://%s:%s/";
    public static final String MAIN_FONT_NAME = "Arial";
    public static final Color CRATE_COLOR = new Color(66, 188, 245);
    public static final Font REMARK_FONT = new Font(MAIN_FONT_NAME, Font.BOLD, 16);
    public static final Dimension SQL_CONNECTION_MANAGER_HEIGHT = new Dimension(0, 200);
    public static final Dimension COMMAND_BOARD_MANAGER_HEIGHT = new Dimension(0, 350);
    public static final String COMMAND_BOARD_MANAGER_STORE = "command_board.json";
    public static final String SQL_CONNECTION_MANAGER_STORE = "connections.json";

    public static final String ERROR_HEADER = "======= Error =======\n";
    public static final Font ERROR_FONT = new Font(MAIN_FONT_NAME, Font.BOLD, 14);
    public static final Color ERROR_FONT_COLOR = new Color(189, 4, 4); // blood

    public static final Color TABLE_HEADER_FONT_COLOR = Color.BLACK;
    public static final Color TABLE_GRID_COLOR = CRATE_COLOR.darker().darker().darker();
    public static final Color TABLE_FOOTER_FONT_COLOR = Color.BLACK;
    public static final Font TABLE_HEADER_FONT = new Font(MAIN_FONT_NAME, Font.BOLD, 18);
    public static final Font TABLE_CELL_FONT = new Font(MAIN_FONT_NAME, Font.PLAIN, 16);
    public static final Font TABLE_FOOTER_FONT = new Font(MAIN_FONT_NAME, Font.PLAIN, 14);
    public static final int TABLE_HEADER_HEIGHT = 50;
    public static final int TABLE_ROW_HEIGHT = 26;
    public static final int TABLE_CELL_MIN_WIDTH = 300;
    public static final int TABLE_CELL_CHAR_WIDTH = 15;

    public static final Color COMMAND_BOARD_FONT_COLOR = Color.WHITE;
    public static final Color COMMAND_BOARD_KEYWORD_FONT_COLOR = CRATE_COLOR;
    public static final Color COMMAND_BOARD_BACKGROUND_COLOR = Color.BLACK;
    public static final Color COMMAND_BOARD_CARET_COLOR = Color.GREEN;
    public static final Font COMMAND_BOARD_HEADER_FONT = new Font(MAIN_FONT_NAME, Font.BOLD, 16);
    public static final Font COMMAND_BOARD_BODY_FONT = new Font(MAIN_FONT_NAME, Font.BOLD, 18);
    public static final Border COMMAND_BOARD_CONNECTED_BORDER = BorderFactory.createLineBorder(CRATE_COLOR, 4, true);
    public static final Border COMMAND_BOARD_DISCONNECTED_BORDER = BorderFactory.createLineBorder(Color.BLACK, 2, true);
    public static final Border COMMAND_BOARD_UNSELECTED_BORDER = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, false);


    public static void copyToClipboard(String text) {
        getSystemClipboard().setContents(new StringSelection(text), null);
    }

    public static void pasteFromClipboard() throws AWTException {
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_V);
    }

    public static String getFromClipboard() throws Exception {
        Clipboard systemClipboard = getSystemClipboard();
        DataFlavor dataFlavor = DataFlavor.stringFlavor;
        if (systemClipboard.isDataFlavorAvailable(dataFlavor)) {
            return (String) systemClipboard.getData(dataFlavor);
        }
        return null;
    }

    private static Clipboard getSystemClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    public static void invokeLater(Runnable ... runnable) {
        if (EventQueue.isDispatchThread()) {
            for (Runnable r : runnable) {
                if (null != r) {
                    r.run();
                }
            }
        } else {
            try {
                EventQueue.invokeLater(() -> {
                    for (Runnable r : runnable) {
                        if (null != r) {
                            r.run();
                        }
                    }
                });
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    private GUIToolkit() {
        throw new IllegalStateException("not meant to me instantiated");
    }
}
