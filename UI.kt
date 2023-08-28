import javax.swing.*
import javax.swing.event.CaretListener
import javax.swing.event.CaretEvent
import java.awt.*
import java.beans.Expression
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.*
import java.awt.GraphicsEnvironment
import javax.swing.border.EmptyBorder

class ImprovedGUI : JFrame("Rich Text Box Example") {
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

        // Open menu item action
        openMenuItem.addActionListener { openFile(textPane) }

        // Save menu item action
        saveMenuItem.addActionListener { saveFile(textPane) }

        // New menu item action
        newMenuItem.addActionListener {
            textPane.text = ""
            fileNameLabel.text = "Untitled"
        }

        // Exit menu item action
        exitMenuItem.addActionListener { System.exit(0) }

        // Font menu item action
        fontMenuItem.addActionListener {
            val fontDialog = FontDialog(textPane)
            fontDialog.isVisible = true
            val selectedFont = fontDialog.selectedFont
            if (selectedFont != null) {
                textPane.font = selectedFont
            }
        }

        // Text color menu item action
        textColorMenuItem.addActionListener {
            val color = JColorChooser.showDialog(this, "Choose a color", Color.BLACK)
            textPane.foreground = color
        }

        // Spacing menu item action
        spacingMenuItem.addActionListener {
            val spacingDialog = SpacingDialog(textPane)
            spacingDialog.isVisible = true
        }

        fileMenu.add(newMenuItem)
        fileMenu.add(openMenuItem)
        fileMenu.add(saveMenuItem)
        fileMenu.add(exitMenuItem)

        editMenu.add(editorMenu)

        styleMenu.add(fontMenuItem)
        styleMenu.add(textColorMenuItem)

        editorMenu.add(spacingMenuItem)

        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(styleMenu)

        return menuBar
    }

    private fun openFile(textPane: JTextPane) {
        val fileOperations = FileOperations()
        val selectedFile = fileOperations.openFile(textPane)
        if (selectedFile != null) {
            fileNameLabel.text = selectedFile.name
        }
    }
    
    private fun saveFile(textPane: JTextPane) {
        val fileOperations = FileOperations()
        val selectedFile = fileOperations.saveFile(textPane)
        if (selectedFile != null) {
            fileNameLabel.text = selectedFile.name
        }
    }
}

object FontLoader {
    val fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
}

class FontDialog(private val textPane: JTextPane) : JDialog() {
    private val fontList = FontLoader.fontList
    var selectedFont: Font? = null

    init {
        title = "Choose Font"
        setSize(400, 200)
        isModal = true
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        val fontPreview = JLabel("Font Preview", JLabel.CENTER)
        fontPreview.font = textPane.font
        add(fontPreview, BorderLayout.CENTER)

        val fontComboBox = JComboBox(fontList)
        fontComboBox.addActionListener {
            val selectedFontName = fontComboBox.selectedItem as String
            val fontSize = fontPreview.font.size
            val newFont = Font(selectedFontName, Font.PLAIN, fontSize)
            fontPreview.font = newFont
            fontPreview.repaint()
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

class SpacingDialog(private val textPane: JTextPane) : JDialog() {
    init {
        title = "Set Spacing"
        setSize(200, 150)
        isModal = true
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        val lineSpacing = (textPane.font.size * 0.5).toFloat()

        val spinnerModel = SpinnerNumberModel(lineSpacing.toDouble(), 0.0, 10.0, 0.1)
        val spinner = JSpinner(spinnerModel)

        spinner.addChangeListener {
            val value = spinner.value as Double
            textPane.styledDocument.setParagraphAttributes(0, textPane.document.length, SimpleAttributeSet().apply {
                StyleConstants.setLineSpacing(this, value.toFloat())
            }, false)
        }

        val buttonPanel = JPanel()
        val okButton = JButton("OK")
        okButton.addActionListener {
            dispose()
        }
        buttonPanel.add(spinner)
        buttonPanel.add(okButton)

        add(buttonPanel, BorderLayout.SOUTH)
    }
}

class LineAndCaretPositionCaretListener(private val textPane: JTextPane, private val lineLabel: JLabel, private val caretPositionLabel: JLabel) : CaretListener {
    private var currentLine = 0
    
    override fun caretUpdate(e: CaretEvent?) {
        if (e != null) {
            val dot = e.dot
            val doc = textPane.document
            if (doc is StyledDocument) {
                val rootElement = doc.defaultRootElement
                val line = rootElement.getElementIndex(dot)
                val startOfLineOffset = rootElement.getElement(line).startOffset
                val column = dot - startOfLineOffset
    
                if (line != currentLine) {
                    lineLabel.text = "Line: ${line + 1}"
                    currentLine = line
                }
                caretPositionLabel.text = "Column: ${column + 1}"
            }
        }
    }
}

class FileOperations {
    fun openFile(textPane: JTextPane): File? {
        val fileChooser = JFileChooser()
        val filter = FileNameExtensionFilter("Text Files", "txt", "doc", "docx", "rtf", "odt")
        fileChooser.fileFilter = filter

        val result = fileChooser.showOpenDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
val selectedFile = fileChooser.selectedFile
            val extension = selectedFile.extension
            when (extension) {
                "txt" -> textPane.text = selectedFile.readText()
                "doc", "docx", "rtf", "odt" -> {
                    // Use appropriate method or library to read these file types
                }
            }
            return selectedFile
        }
        return null
    }

    fun saveFile(textPane: JTextPane): File? {
        val fileChooser = JFileChooser()

        val result = fileChooser.showSaveDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
val extension = selectedFile.extension
when (extension) {
    "txt" -> selectedFile.writeText(textPane.text)
    "doc", "docx", "rtf", "odt" -> {
        // Use appropriate method or library to write these file types
    }
}
            return selectedFile
        }
        return null
    }
}

fun main() {
    ImprovedGUI()
}
