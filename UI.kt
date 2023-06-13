import javax.swing.*
import javax.swing.event.CaretListener
import javax.swing.event.CaretEvent
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.beans.Expression
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.border.EmptyBorder
import javax.swing.text.*

class GUI : JFrame("Rich Text Box Example") {
    private val textPane: JTextPane = JTextPane()
    private val fileNameLabel: JLabel = JLabel("Untitled")
    private val lineCounter: JLabel = JLabel("Line: 1")
    private val caretPositionCounter: JLabel = JLabel("Column: 0")

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(500, 500)

        textPane.document = createStyledDocument()

        val scrollPane = JScrollPane(textPane)
        add(scrollPane)

        val counterPanel = JPanel()
        val counterPanelLayout = BoxLayout(counterPanel, BoxLayout.X_AXIS)
        counterPanel.layout = counterPanelLayout
        counterPanel.add(lineCounter)
        counterPanel.add(Box.createRigidArea(Dimension(10, 0))) // Add spacing
        counterPanel.add(caretPositionCounter)
        counterPanel.add(Box.createHorizontalGlue()) // Flexible space
        counterPanel.add(Box.createRigidArea(Dimension(10, 0))) // Add spacing
        counterPanel.add(fileNameLabel)

        val panel = JPanel(BorderLayout())
        panel.add(counterPanel, BorderLayout.WEST)
        panel.border = EmptyBorder(5, 5, 5, 5) // Add padding
        add(panel, BorderLayout.SOUTH)

        // Register the caret listener
        textPane.addCaretListener(LineAndCaretPositionCaretListener(textPane, lineCounter, caretPositionCounter))

        val menuBar = createMenuBar(textPane)
        jMenuBar = menuBar

        isVisible = true
    }

    private fun createStyledDocument(): StyledDocument {
        val doc = DefaultStyledDocument()
        val style = doc.addStyle("DefaultStyle", null)
        StyleConstants.setFontFamily(style, "Arial")
        StyleConstants.setFontSize(style, 12)

        return doc
    }

    private fun createMenuBar(textPane: JTextPane): JMenuBar {
        val menuBar = JMenuBar()

        val fileMenu = JMenu("File")
        val editMenu = JMenu("Edit")
        val styleMenu = JMenu("Style")

        val openMenuItem = JMenuItem("Open")
        val saveMenuItem = JMenuItem("Save")
        val newMenuItem = JMenuItem("New") // New menu item
        val exitMenuItem = JMenuItem("Exit") // Exit menu item

        val fontMenuItem = JMenuItem("Font")
        val textColorMenuItem = JMenuItem("Text Color")

        val editorMenu = JMenu("Editor")
        val spacingMenuItem = JMenuItem("Spacing")

        openMenuItem.addActionListener {
            // Handle Open action
            val fileChooser = JFileChooser()
            val filter = FileNameExtensionFilter("Text files", "txt")
            fileChooser.fileFilter = filter
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                val text = file.readText()
                textPane.text = text
                fileNameLabel.text = file.name
            }
        }

        saveMenuItem.addActionListener {
            // Handle Save action
            val fileChooser = JFileChooser()
            val filter = FileNameExtensionFilter("Text files", "txt")
            fileChooser.fileFilter = filter
            val result = fileChooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                file.writeText(textPane.text)
                fileNameLabel.text = file.name
            }
        }

        newMenuItem.addActionListener {
            // Handle New action
            textPane.text = ""
            fileNameLabel.text = "Untitled"
        }

        exitMenuItem.addActionListener {
            // Handle Exit action
            System.exit(0)
        }

        fontMenuItem.addActionListener {
            // Handle Font action
            val fontDialog = FontDialog(textPane.font)
            fontDialog.isVisible = true
            val selectedFont = fontDialog.selectedFont
            if (selectedFont != null) {
                textPane.font = selectedFont
            }
        }

        textColorMenuItem.addActionListener {
            // Handle Text Color action
            val colorChooser = JColorChooser()
            val result = Expression(colorChooser, "showDialog", arrayOf(null, "Choose Text Color", textPane.foreground)).value as Color?
            if (result != null) {
                textPane.foreground = result
            }
        }

        spacingMenuItem.addActionListener {
            // Handle Spacing action
            val spacingFrame = JFrame("Spacing")
            spacingFrame.setSize(400, 200)
            spacingFrame.setLocationRelativeTo(null)
            spacingFrame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val spacingPanel = JPanel(GridLayout(2, 2))
            val topLabel = JLabel("Top:")
            val topField = JTextField()
            val bottomLabel = JLabel("Bottom:")
            val bottomField = JTextField()
            spacingPanel.add(topLabel)
            spacingPanel.add(topField)
            spacingPanel.add(bottomLabel)
            spacingPanel.add(bottomField)

            val buttonPanel = JPanel()
            val okButton = JButton("OK")
            okButton.addActionListener {
                val top = topField.text.toIntOrNull() ?: 0
                val bottom = bottomField.text.toIntOrNull() ?: 0
                val spacing = SimpleAttributeSet()
                StyleConstants.setSpaceAbove(spacing, top.toFloat())
                StyleConstants.setSpaceBelow(spacing, bottom.toFloat())
                textPane.styledDocument.setParagraphAttributes(0, textPane.styledDocument.length, spacing, false)
                spacingFrame.dispose()
            }
            val cancelButton = JButton("Cancel")
            cancelButton.addActionListener {
                spacingFrame.dispose()
            }
            buttonPanel.add(okButton)
            buttonPanel.add(cancelButton)

            spacingFrame.add(spacingPanel, BorderLayout.CENTER)
            spacingFrame.add(buttonPanel, BorderLayout.SOUTH)
            spacingFrame.isVisible = true
        }

        fileMenu.add(openMenuItem)
        fileMenu.add(saveMenuItem)
        fileMenu.add(newMenuItem) // Add New menu item
        fileMenu.addSeparator()
        fileMenu.add(exitMenuItem) // Add Exit menu item

        styleMenu.add(fontMenuItem)
        styleMenu.add(textColorMenuItem)

        editorMenu.add(spacingMenuItem)

        editMenu.add(editorMenu)

        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(styleMenu)

        return menuBar
    }

    class LineAndCaretPositionCaretListener(
            private val textPane: JTextPane,
            private val lineCounter: JLabel,
            private val caretPositionCounter: JLabel
    ) : CaretListener {
        override fun caretUpdate(e: CaretEvent) {
            try {
                val caretPosition = textPane.caretPosition
                val line = textPane.document.defaultRootElement.getElementIndex(caretPosition) + 1
                val lineStartOffset = textPane.document.defaultRootElement.getElement(line - 1).startOffset
                val caretOffset = caretPosition - lineStartOffset
                lineCounter.text = "Line: $line"
                caretPositionCounter.text = "Column: $caretOffset"
            } catch (ex: Exception) {
                lineCounter.text = "Line: 1"
                caretPositionCounter.text = "Column: 0"
            }
        }
    }

    class FontDialog(private val initialFont: Font) : JDialog() {
        private val fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
        var selectedFont: Font? = null

        init {
            title = "Choose Font"
            setSize(400, 200)
            isModal = true
            setLocationRelativeTo(null)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val fontPreview = JLabel("Font Preview", JLabel.CENTER)
            fontPreview.font = initialFont
            add(fontPreview, BorderLayout.CENTER)

            val fontComboBox = JComboBox(fontList)
            fontComboBox.addActionListener {
                val selectedFontName = fontComboBox.selectedItem as String
                val fontSize = fontPreview.font.size
                val newFont = Font(selectedFontName, Font.PLAIN, fontSize)
                fontPreview.font = newFont
            }

            val buttonPanel = JPanel()
            val selectButton = JButton("Select")
            selectButton.addActionListener {
                selectedFont = fontPreview.font
                dispose()
            }
            val cancelButton = JButton("Cancel")
            cancelButton.addActionListener {
                dispose()
            }
            buttonPanel.add(fontComboBox)
            buttonPanel.add(selectButton)
            buttonPanel.add(cancelButton)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        GUI()
    }
}
