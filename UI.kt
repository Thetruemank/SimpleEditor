import javax.swing.*
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import javax.swing.event.CaretListener
import javax.swing.event.CaretEvent
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.Style
import javax.swing.text.StyleContext
import javax.swing.border.EmptyBorder

fun main() {
    SwingUtilities.invokeLater {
        createAndShowGUI()
    }
}

private fun createAndShowGUI() {
    val frame = JFrame("Rich Text Box Example")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(500, 500)

    val textPane = JTextPane()
    textPane.document = createStyledDocument()

    val scrollPane = JScrollPane(textPane)
    frame.add(scrollPane)

    val lineCounter = JLabel("Line: 1")
    val caretPositionCounter = JLabel("Column: 0")

    val counterPanel = JPanel(GridLayout(1, 2))
    counterPanel.add(lineCounter)
    counterPanel.add(caretPositionCounter)

    val panel = JPanel(BorderLayout())
    panel.add(counterPanel, BorderLayout.WEST)
    panel.border = EmptyBorder(5, 5, 5, 5) // Add padding
    frame.add(panel, BorderLayout.SOUTH)

    // Register the caret listener
    textPane.addCaretListener(LineAndCaretPositionCaretListener(textPane, lineCounter, caretPositionCounter))

    val menuBar = createMenuBar(textPane)
    frame.jMenuBar = menuBar

    frame.isVisible = true
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
    val exitMenuItem = JMenuItem("Exit")

    val fontMenuItem = JMenuItem("Font")
    val textColorMenuItem = JMenuItem("Text Color")

    openMenuItem.addActionListener {
        // Handle Open action
        val fileChooser = JFileChooser()
        val filter = FileNameExtensionFilter("Text files", "txt")
        fileChooser.fileFilter = filter

        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            if (selectedFile.extension == "txt") {
                val fileContent = selectedFile.readText()
                textPane.text = fileContent
            } else {
                JOptionPane.showMessageDialog(null, "Please select a .txt file.")
            }
        }
    }

    saveMenuItem.addActionListener {
        // Handle Save action
        val fileChooser = JFileChooser()
        val filter = FileNameExtensionFilter("Text files", "txt")
        fileChooser.fileFilter = filter

        val result = fileChooser.showSaveDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            val fileContent = textPane.text

            if (selectedFile.extension != "txt") {
                selectedFile.renameTo(File(selectedFile.absolutePath + ".txt"))
            }

            selectedFile.writeText(fileContent)
        }
    }

    exitMenuItem.addActionListener {
        // Handle Exit action
        println("Exit program")
        System.exit(0)
    }

    fontMenuItem.addActionListener {
        // Handle Font action
        val fontDialog = FontDialog(textPane.font)
        fontDialog.isVisible = true
        fontDialog.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                val selectedFont = fontDialog.selectedFont
                if (selectedFont != null) {
                    val doc = textPane.styledDocument
                    val style = doc.addStyle("SelectedFontStyle", null)
                    StyleConstants.setFontFamily(style, selectedFont.fontName)
                    StyleConstants.setFontSize(style, selectedFont.size)
                    doc.setCharacterAttributes(0, doc.length, style, false)
                }
            }
        })
    }

    textColorMenuItem.addActionListener {
        // Handle Text Color action
        val color = JColorChooser.showDialog(textPane, "Choose Text Color", Color.BLACK)
        color?.let {
            textPane.foreground = color
        }
    }

    fileMenu.add(openMenuItem)
    fileMenu.add(saveMenuItem)
    fileMenu.addSeparator()
    fileMenu.add(exitMenuItem)

    styleMenu.add(fontMenuItem)
    styleMenu.add(textColorMenuItem)

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
