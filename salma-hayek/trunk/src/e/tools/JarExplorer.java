package e.tools;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;
import javax.swing.*;

import e.util.*;

/**
 * Shows and decodes the contents of JAR files specified on the command line.
 * Basically just a simple front end to jar(1) and javap(1).
 * 
 * @author Elliott Hughes
 */
public class JarExplorer extends JFrame {
    private JCheckBox showLineNumberAndLocalVariableTables;
    private JCheckBox showVerboseDetail;
    private JList list;
    private DefaultListModel model;

    private JTextArea summaryTextArea;
    private JTextArea detailTextArea;

    private String filename;

    public JarExplorer(String filename) {
        super(filename);
        this.filename = filename;

        try {
            initFromJarFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        setContentPane(makeUi());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initFromJarFile() throws IOException {
        File file = new File(filename);
        ZipFile zipFile = new ZipFile(file);

        final int totalEntryCount = zipFile.size();
        int entryCount = 0;

        ArrayList entries = new ArrayList();
        Enumeration e = zipFile.entries();
        while (e.hasMoreElements()) {
            ++entryCount;
            ZipEntry entry = (ZipEntry) e.nextElement();
            if (entry.isDirectory()) {
                continue;
            }

            entries.add(entry.getName());
        }
        zipFile.close();

        Collections.sort(entries);
        model = new DefaultListModel();
        for (int i = 0; i < entries.size(); ++i) {
            model.addElement(entries.get(i));
        }
    }

    private JComponent makeUi() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new javax.swing.border.EmptyBorder(0, 10, 10, 10));

        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showLineNumberAndLocalVariableTables = new JCheckBox("Show Line Number Tables");
        checkBoxPanel.add(showLineNumberAndLocalVariableTables);
        showVerboseDetail = new JCheckBox("Show Verbose Information");
        checkBoxPanel.add(showVerboseDetail);
        panel.add(checkBoxPanel, BorderLayout.NORTH);

        list = new JList(model);
        list.setVisibleRowCount(10);
        JScrollPane entriesScroller = new JScrollPane(list);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick((String) list.getSelectedValue());
                }
            }
        });

        summaryTextArea = makeTextArea();
        detailTextArea = makeTextArea();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Summary", new JScrollPane(summaryTextArea));
        tabbedPane.add("Detail", new JScrollPane(detailTextArea));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              entriesScroller, tabbedPane);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JTextArea makeTextArea() {
        JTextArea textArea = new JTextArea(30, 80);
        textArea.setEditable(false);
        textArea.setDragEnabled(false);
        return textArea;
    }

    private void handleDoubleClick(String entry) {
        if (entry.endsWith(".class")) {
            String className = entry.replace('/', '.');
            className = className.replaceAll("\\.class$", "");

            summaryTextArea.setText(runJavaP(className, false));
            summaryTextArea.setCaretPosition(0);

            detailTextArea.setText(runJavaP(className, true));
            detailTextArea.setCaretPosition(0);
        }
    }

    private String runJavaP(String className, boolean detail) {
        ArrayList lines = new ArrayList();
        ArrayList errors = new ArrayList();

        ArrayList command = new ArrayList();
        command.add("javap");
        command.add("-private");
        if (detail) {
            command.add("-c");
            if (showLineNumberAndLocalVariableTables.isSelected()) {
                command.add("-l");
            }
            if (showVerboseDetail.isSelected()) {
                command.add("-verbose");
            }
        }
        command.add("-classpath");
        command.add(filename);
        command.add(className);

        ProcessUtilities.backQuote(null, makeArray(command), lines, errors);

        List source = (errors.size() == 0) ? lines : errors;
        return StringUtilities.join(source, "\n");
    }

    private String[] makeArray(final List list) {
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static void main(String[] filenames) {
        for (int i = 0; i < filenames.length; ++i) {
            final String filename = filenames[i];
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new JarExplorer(filename);
                }
            });
        }
    }
}
