package com.github.baroncyrus.aicodehelper.settings;

import com.github.baroncyrus.aicodehelper.constant.Constants;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class PluginConfigPanel {
    private JPanel mainPanel;
    private ComboBox<String> clientNameField;
    private ComboBox<String> modelComboBox;
    private ComboBox<String> languageComboBox;

    private JButton configButton;

    private JPanel clientPanel;

    public PluginConfigPanel() {
        initComponents();
        layoutComponents();
        setupListeners();
    }

    private void initComponents() {
        clientNameField = new ComboBox<>(Constants.LLM_CLIENTS);
        modelComboBox = new ComboBox<>();
        modelComboBox.setEditable(true);
        languageComboBox = new ComboBox<>(Constants.languages);
        //promptTypeComboBox = new ComboBox<>(Constants.getAllPromptTypes());
        //customPromptsTableModel = new DefaultTableModel(new String[]{"Description", "Prompt"}, 0);
        //customPromptsTable = new JBTable(customPromptsTableModel);

        // 设置 Description 列的首选宽度和最大宽度
        //customPromptsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        //customPromptsTable.getColumnModel().getColumn(0).setMaxWidth(200);

        // 设置 Prompt 列可以自由伸展
        //customPromptsTable.getColumnModel().getColumn(1).setPreferredWidth(400);

        //customPromptPanel = createCustomPromptPanel();
        //projectPromptPanel = createProjectPromptPanel();

        configButton = new JButton(AllIcons.General.Settings);
        configButton.setToolTipText("Configure Module Settings");

        // 创建包含Stream支持状态的面板
        clientPanel = new JPanel(new BorderLayout(5, 0));
        clientPanel.add(clientNameField, BorderLayout.CENTER);

        // 添加Stream状态标签
        JLabel streamLabel = new JLabel();
        streamLabel.setForeground(JBColor.GRAY);
        clientPanel.add(streamLabel, BorderLayout.EAST);


        // 添加客户端选择监听器
        clientNameField.addActionListener(e -> {
            String selectedClient = (String) clientNameField.getSelectedItem();
            updateModuleComboBox(selectedClient);
        });

        // 初始化模块下拉框
        updateModuleComboBox((String) clientNameField.getSelectedItem());
    }

    private void layoutComponents() {
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add Report Bug link in the top right corner
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel reportBugLabel = new JLabel("<html><a href='https://github.com/BaronCyrus/ai_code_helper/issues'>Report Bug ↗</a></html>");
        reportBugLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reportBugLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/BaronCyrus/ai_code_helper/issues"));
                } catch (Exception ignored) {}
            }
        });
        topPanel.add(reportBugLabel, BorderLayout.EAST);
        gbc.gridwidth = 2;
        addComponent(topPanel, gbc, 0, 0, 1.0);

        // Reset gridwidth for subsequent components
        gbc.gridwidth = 1;

        addComponent(new JBLabel("LLM Client:"), gbc, 0, 1, 0.0);
        addComponent(clientPanel, gbc, 1, 1, 1.0);

        JPanel modulePanel = new JPanel(new BorderLayout(5, 0));
        modulePanel.add(modelComboBox, BorderLayout.CENTER);
        modulePanel.add(configButton, BorderLayout.EAST);

        // Create module label panel with help icon
        JPanel moduleLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JBLabel moduleLabel = new JBLabel("Module: ");
        JBLabel moduleHelpIcon = new JBLabel(AllIcons.General.ContextHelp);
        moduleHelpIcon.setToolTipText("You can input custom module name(this is a editable comboBox)");
        moduleLabelPanel.add(moduleLabel);
        moduleLabelPanel.add(moduleHelpIcon);

        addComponent(moduleLabelPanel, gbc, 0, 2, 0.0);
        addComponent(modulePanel, gbc, 1, 2, 1.0);

        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JBLabel languageLabel = new JBLabel("Language: ");
        JBLabel helpIcon = new JBLabel(AllIcons.General.ContextHelp);
        helpIcon.setToolTipText("The language of the generated commit message");
        languagePanel.add(languageLabel);
        languagePanel.add(helpIcon);

        addComponent(languagePanel, gbc, 0, 3, 0.0);
        addComponent(languageComboBox, gbc, 1, 3, 1.0);

        //addComponent(new JBLabel("Prompt type:"), gbc, 0, 4, 0.0);
        //addComponent(promptTypeComboBox, gbc, 1, 4, 1.0);

        // Create a panel to maintain consistent height
        //JPanel contentPanel = new JPanel(new CardLayout());
        //contentPanel.setPreferredSize(new Dimension(-1, 200)); // 设置固定高度
        //contentPanel.add(customPromptPanel, "CUSTOM_PROMPT");
        //contentPanel.add(projectPromptPanel, "PROJECT_PROMPT");

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        //mainPanel.add(contentPanel, gbc);

//        promptTypeComboBox.addActionListener(e -> {
//            CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
//            if (Constants.CUSTOM_PROMPT.equals(promptTypeComboBox.getSelectedItem())) {
//                cardLayout.show(contentPanel, "CUSTOM_PROMPT");
//            } else {
//                cardLayout.show(contentPanel, "PROJECT_PROMPT");
//            }
//        });
    }

    private void addComponent(Component component, GridBagConstraints gbc, int gridx, int gridy, double weightx) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = weightx;
        mainPanel.add(component, gbc);
    }

    private void setupListeners() {
        clientNameField.addActionListener(e -> {
            String selectedClient = (String) clientNameField.getSelectedItem();
            updateModuleComboBox(selectedClient);
        });

        configButton.addActionListener(e -> showModuleConfigDialog());
    }

    private void showModuleConfigDialog() {
        String selectedClient = (String) clientNameField.getSelectedItem();
        String selectedModule = (String) modelComboBox.getSelectedItem();

        ModuleConfigDialog dialog = new ModuleConfigDialog(mainPanel,selectedClient,selectedModule);
        dialog.show();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    private void updateModuleComboBox(String selectedClient) {
        modelComboBox.removeAllItems();
        String[] modules = Constants.CLIENT_MODULES.get(selectedClient);
        if (modules != null) {
            for (String module : modules) {
                modelComboBox.addItem(module);
            }
        }
    }


    public String getSelectedModel() {
        return (String) modelComboBox.getSelectedItem();
    }

    public JComboBox<String> getClientComboBox() {
        return clientNameField;
    }
}